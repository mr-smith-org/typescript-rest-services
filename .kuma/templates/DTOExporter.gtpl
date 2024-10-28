{{range .data}}import type { {{ toPascalCase . }} } from "./{{ toSnakeCase . }}" 
{{end}}

export type {
{{range .data}}{{toPascalCase .}},
{{end -}}
}