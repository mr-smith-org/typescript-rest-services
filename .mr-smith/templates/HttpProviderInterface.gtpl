/**
 * Supported HTTP methods.
 */
export type HttpMethod =
  | "get"
  | "post"
  | "put"
  | "delete"
  | "patch"
  | "options"
  | "head"
  | "trace";

/**
 * Retry options for HTTP requests.
 */
export interface RetryOptions {
  retries: number; // Number of retry attempts
  delay: number;   // Initial delay between retries in milliseconds
  factor?: number; // Optional factor for exponential backoff
}

/**
 * Generic configuration for HTTP requests.
 */
export interface RequestConfig<C> {
  headers?: Record<string, string>;
  timeout?: number; // Timeout duration in milliseconds
  retry?: RetryOptions; // Retry configuration
  config?: C; // Additional configuration for the specific HTTP provider
}

/**
 * Generic response structure.
 */
export interface HttpResponse<T> {
  status: number;
  statusText: string;
  headers: Record<string, string>;
  data: T;
}

/**
 * HTTP Provider interface to abstract HTTP requests.
 */
export interface IHttpProvider<C> {
  /**
   * Makes an HTTP request.
   * @param method HTTP method (GET, POST, etc.)
   * @param url Endpoint URL, possibly with placeholders (e.g., /users/{id})
   * @param data Optional request data including query, params, and body
   * @param config Optional request configuration
   * @returns A promise resolving to an HttpResponse containing the response data
   */
  request<T>(
    method: HttpMethod,
    url: string,
    data?: RequestData,
    config?: RequestConfig<C>
  ): Promise<HttpResponse<T>>;
}

export type RequestData = {
  query?: any;
  body?: any;
  params?: any;
};

export interface HttpError extends Error {
  status?: number;
  statusText?: string;
  data?: any;
  code?: string;
}
