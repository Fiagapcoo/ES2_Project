
//testes para o endpoint health (Metodo GET)
pm.test("Verifica método HTTP da requisição", function () {

    const metodo = pm.request.method;
    console.log("metodo usado:",metodo);
    switch(metodo){
    case "GET":
            pm.response.to.have.status(200);
            break;
    case "POST":
            pm.response.to.have.status(404);
            break;
    case "PUT":
            pm.response.to.have.status(404);   
            break;
    case "DELETE":
            pm.response.to.have.status(404);
            break;
     }
});


pm.test("Estrutura correta por método HTTP", function () {
    const metodo = pm.request.method;

    switch (metodo) {
        case "GET":
            const jsonData = pm.response.json();

            pm.expect(pm.response.code).to.eql(200);
            pm.expect(jsonData).to.have.property("status", "UP");
            pm.expect(jsonData).to.have.property("timestamp");

            const timestamp = jsonData.timestamp;
            const isoRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/;
            pm.expect(timestamp).to.match(isoRegex);
            break;

        case "POST":
        case "PUT":
        case "DELETE":
            const html = pm.response.text();

            // Teste para resposta de erro HTML padrão do Express
            pm.expect(pm.response.code).to.be.oneOf([404, 405]);
            pm.expect(html).to.include("<!DOCTYPE html>");
            pm.expect(html).to.include("<title>Error</title>");
            pm.expect(html).to.include(`<pre>Cannot ${metodo} /health</pre>`);
            break;

        default:
            pm.expect.fail(`Método HTTP '${metodo}' não tratado no script`);
    }
});



