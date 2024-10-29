import type { RequestData, HttpError, IHttpProvider, HttpMethod, HttpResponse, RequestConfig } from "./http_provider_interface";

/**
 * Base implementation of IHttpProvider.
 */
export abstract class HttpProvider<C> implements IHttpProvider<C> {
  protected baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL.endsWith("/") ? baseURL.slice(0, -1) : baseURL;
  }

  /**
   * Replaces placeholders in the URL with actual values.
   */
  protected replaceVars(url: string, vars: Record<string, any>): string {
    return url.replace(/{([^{}]*)}/g, (_, key) => {
      const value = vars[key];
      if (value === undefined || value === null) {
        throw new Error(`Missing value for URL variable: ${key}`);
      }
      return encodeURIComponent(String(value));
    });
  }

  /**
   * Determines if an error is retryable.
   * @param error The standardized HttpError
   * @returns Boolean indicating if the error is retryable
   */
  protected isRetryable(error: HttpError): boolean {
    // Retry on network errors, timeouts, or 5xx server errors
    return (
      error.name === 'AbortError' || // Fetch-specific timeout error
      error.code === 'ECONNABORTED' || // Axios-specific timeout error
      !error.status || // Network errors (no response received)
      (error.status >= 500 && error.status < 600) // Server-side errors
    );
  }

  abstract request<T>(
    method: HttpMethod,
    url: string,
    data?: RequestData,
    config?: RequestConfig<C>
  ): Promise<HttpResponse<T>>;
}
