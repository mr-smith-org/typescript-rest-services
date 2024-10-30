
# Swagger-Based Code Generator (TypeScript)

Create rapidly fully type-safe services that consume REST APIs through a Swagger/OpenAPI 2.0 file.

```bash
kuma exec module -m kuma-typescript-rest-services -r create-services 
```

![out](https://github.com/user-attachments/assets/6a73274f-7bf1-4b65-8f22-3eb4d6d6d067)


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
### Kuma docs 
https://kuma-framework.vercel.app/
