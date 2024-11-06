import type { HttpError, RequestData, HttpMethod, HttpResponse, RequestConfig, RetryOptions } from "./http_provider_interface";
import { HttpProvider } from "./http_provider";

/**
 * Fetch-based implementation of IHttpProvider.
 */
export class FetchHttpProvider<C> extends HttpProvider<C> {
  constructor(baseURL: string) {
    baseURL = baseURL.endsWith("/") ? baseURL.slice(0, -1) : baseURL;
    super(baseURL);
  }

  /**
   * Builds a query string from an object.
   * @param query The query parameters as an object
   * @returns The serialized query string (e.g., ?search=term)
   */
  private buildQueryString(query: Record<string, any>): string {
    const queryParams = new URLSearchParams();
    for (const key in query) {
      const value = query[key];
      if (value !== undefined && value !== null) {
        queryParams.append(key, String(value));
      }
    }
    const queryString = queryParams.toString();
    return queryString ? `?${queryString}` : "";
  }

  /**
   * Converts Headers to a plain object.
   * @param headers The Headers instance from the response
   * @returns A plain object with header key-value pairs
   */
  private headersToObject(headers: Headers): Record<string, string> {
    const result: Record<string, string> = {};
    headers.forEach((value, key) => {
      result[key] = value;
    });
    return result;
  }

  /**
   * Delays execution for the specified duration.
   * @param ms Milliseconds to delay
   */
  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }


  async request<T>(
    method: HttpMethod,
    url: string,
    data?: RequestData,
    config?: RequestConfig<C>
  ): Promise<HttpResponse<T>> {
    let finalURL = `${this.baseURL}${url}`;
    if (data?.params) {
      finalURL = this.replaceVars(finalURL, data.params);
    }

    if (data?.query) {
      const queryString = this.buildQueryString(data.query);
      finalURL += queryString;
    }

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(config?.headers || {}),
    };

    const fetchOptions: RequestInit = {
      method,
      headers,
    };

    if (data?.body && ["post", "put", "patch", "delete"].includes(method)) {
      fetchOptions.body = JSON.stringify(data.body);
    }

    const timeout = config?.timeout || 5000;

    const retryOptions: RetryOptions = config?.retry || { retries: 0, delay: 1000, factor: 2 };

    let attempt = 0;
    let currentDelay = retryOptions.delay;

    while (attempt <= retryOptions.retries) {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), timeout);
      fetchOptions.signal = controller.signal;

      try {
        const response = await fetch(finalURL, fetchOptions);

        clearTimeout(timeoutId);

        let responseData: any;
        const contentType = response.headers.get("Content-Type");
        if (contentType && contentType.includes("application/json")) {
          responseData = await response.json();
        } else {
          responseData = await response.text();
        }

        if (!response.ok) {
          const error: HttpError = new Error(`HTTP Error: ${response.status} ${response.statusText}`) as HttpError;
          error.status = response.status;
          error.statusText = response.statusText;
          error.data = responseData;
          throw error;
        }

        const responseHeaders = this.headersToObject(response.headers);

        return {
          status: response.status,
          statusText: response.statusText,
          headers: responseHeaders,
          data: responseData as T,
        };
      } catch (error: any) {
        clearTimeout(timeoutId);

        const standardizedError: HttpError = error instanceof Error ? error : new Error(String(error)) as HttpError;
        standardizedError.status = error.status;
        standardizedError.statusText = error.statusText;
        standardizedError.data = error.data;
        standardizedError.code = error.code;

        if (this.isRetryable(standardizedError) && attempt < retryOptions.retries) {
          await this.delay(currentDelay);
          currentDelay = retryOptions.factor ? currentDelay * retryOptions.factor : currentDelay;
          attempt++;
          continue;
        }

        throw standardizedError;
      }
    }

    throw new Error('Request failed after maximum retries');
  }
}
