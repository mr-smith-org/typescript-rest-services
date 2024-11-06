
# Swagger-Based Code Generator (TypeScript)

Create rapidly fully type-safe services that consume REST APIs through a Swagger/OpenAPI 2.0 file.

```bash
mr exec module -m typescript-rest-services -r create-services 
```
![out](https://github.com/user-attachments/assets/d4de27d0-1879-4585-b65b-468f8ca79cf6)

## Usage 

### With axios

```ts
const services = buildServices(new AxiosHttpProvider("https://petstore.swagger.io/v2/"));
services.pet.getPetById({ params: { petId: 1 } }).then(result => console.log(result.data));
```

### With fetch API

```ts
const services = buildServices(new FetchHttpProvider("https://petstore.swagger.io/v2/"));
services.pet.getPetById({ params: { petId: 1 } }).then(result => console.log(result.data));
```
### Mr. Smith docs 
[https://mr-smith.site](https://mr-smith.site)
