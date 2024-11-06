import axios, { AxiosInstance, AxiosError, AxiosResponse } from "axios";
import type { RequestData, HttpError, HttpMethod, HttpResponse, RequestConfig, RetryOptions } from "./http_provider_interface";
import { HttpProvider } from "./http_provider";

/**
 * Axios-based implementation of IHttpProvider.
 */
export class AxiosHttpProvider<C> extends HttpProvider<C> {
  private client: AxiosInstance;

  constructor(baseURL: string) {
    baseURL = baseURL.endsWith("/") ? baseURL.slice(0, -1) : baseURL;
    super(baseURL);
    this.client = axios.create({ baseURL });
  }

  /**
   * Converts AxiosResponse to HttpResponse.
   */
  private convertResponse<T>(response: AxiosResponse<T>): HttpResponse<T> {
    const headers: Record<string, string> = {};
    for (const key in response.headers) {
      headers[key] = response.headers[key];
    }

    return {
      status: response.status,
      statusText: response.statusText,
      headers,
      data: response.data as T,
    };
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

    const axiosConfig: any = {
      ...config?.config,
      method,
      url: finalURL,
      headers: {
        'Content-Type': 'application/json',
        ...(config?.headers || {}),
      },
      timeout: config?.timeout
    };

    if (data?.query) {
      axiosConfig.params = data.query;
    }

    if (data?.body) {
      axiosConfig.data = data.body;
    }

    const retryOptions: RetryOptions = config?.retry || { retries: 0, delay: 1000, factor: 2 };
    let attempt = 0;
    let currentDelay = retryOptions.delay;

    while (attempt <= retryOptions.retries) {
      try {
        const response = await this.client.request(axiosConfig);
        return this.convertResponse<T>(response);
      } catch (error: any) {
        const axiosError = error as AxiosError;

        // Standardize the error
        const standardizedError: HttpError = {
          name: axiosError.name,
          message: axiosError.message,
          status: axiosError.response?.status,
          statusText: axiosError.response?.statusText,
          data: axiosError.response?.data,
          code: axiosError.code,
        };

        if (this.isRetryable(standardizedError) && attempt < retryOptions.retries) {
          await this.delay(currentDelay);
          currentDelay = retryOptions.factor ? currentDelay * retryOptions.factor : currentDelay;
          attempt++;
          continue;
        }

        if (standardizedError.status) {
          // Attach response data to the error
          const errorWithResponse: HttpError = new Error(`HTTP Error: ${standardizedError.status} ${standardizedError.statusText}`);
          errorWithResponse.status = standardizedError.status;
          errorWithResponse.statusText = standardizedError.statusText;
          errorWithResponse.data = standardizedError.data;
          throw errorWithResponse;
        } else {
          throw standardizedError;
        }
      }
    }

    throw new Error('Request failed after maximum retries');
  }
}
