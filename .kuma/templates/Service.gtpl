{{- $refs := getRefsList .data.operations -}}
{{- if $refs -}}
import type { 
  {{ range $key, $value := getRefsList .data.operations -}}
{{ toPascalCase $value }},
  {{ end -}}
} from '../../dto'
{{- end -}}
{{"\n"}}
import type { 
  IHttpProvider, 
  RequestConfig, 
  HttpResponse
 } from '../../providers/http/http_provider_interface'

export class {{toPascalCase .data.name}}Service {
  private http: IHttpProvider;

  constructor(http: IHttpProvider) {
    this.http = http;
  }
  {{"\n"}}
  {{- range $path, $op :=  .data.operations -}}
  {{ range  $method, $data := $op }}
    {{- $params := getParamsByType $data.parameters "path" -}}
    {{- $query := getParamsByType $data.parameters "query" -}}
    {{- $body := getParamsByType $data.parameters "body" -}}
    {{ if $data.description }}//{{ $data.description }}{{end}}
    async {{ $data.operationId }}(data?: {
      {{- if $params }}params?: { {{ range $params }}
        {{ .name }}{{- if not .required -}}?{{- end -}}: {{- block "TypeResolver" . }}{{end}},
      {{- end -}} },{{- end -}}
      {{- if $query }}query?: { {{ range $query }}
        {{ .name }}{{- if not .required -}}?{{- end -}}: {{- block "TypeResolver" . }}{{end}},
      {{- end -}} },{{- end -}}
      {{- if $body }}body?: {{ range $body }}
        {{- block "TypeResolver" .schema  }}{{end}},
      {{- end -}}{{- end -}}
    },   config?: RequestConfig):
     
      {{- $responses := list -}}
      {{- range $index, $response := $data.responses -}}
        {{- if $response.schema -}}
          {{- $inResponse := $responses | has $response.schema -}}
          {{- if not $inResponse -}}
            {{- $responses = $responses | append $response.schema -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
    Promise<HttpResponse<{{- block "ResponseTypeResolver" $responses -}}{{end}}>> {
      return await this.http.request<{{- block "ResponseTypeResolver" $responses -}}{{end}}>("{{ toLower $method }}","{{ $path }}", data, config);
    }

  {{end}}
  {{end}}
}