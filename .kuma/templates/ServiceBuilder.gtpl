import type { IHttpProvider } from "../providers/http";
import { {{- range $index, $service := .data}}{{if $index}},{{end}}{{toPascalCase .}}Service{{end}} } from "./";

export const buildServices = <C>(provider: IHttpProvider<C>) => {
  return {
  {{- range .data}}
    {{toCamelCase .}}: new {{toPascalCase .}}Service(provider),
  {{- end -}}
  };
};
