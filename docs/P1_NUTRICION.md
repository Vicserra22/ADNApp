# P1 · Rituales y biblioteca de alimentos

P1 queda implementada sobre la navegación de P0. Se incluyen agua, registro de tiempo exterior, tarjetas nutricionales reversibles y la nevera local.

## Agua

La botella muestra el total del día frente al objetivo configurable de 2.000 ml. Tocar la botella o pulsar «Añadir un vaso» registra 250 ml; también se puede introducir una cantidad exacta. Cada entrada local conserva su identificador y permite deshacerla. Con sesión iniciada se guarda además como entrada `water` del historial nutricional y el identificador remoto queda asociado para que deshacer actualice el agregado del día. Si la red falla, el registro local no se pierde y se informa de que queda pendiente de sincronización.

La botella no limita el dato al tamaño del dibujo. El nivel se satura visualmente al 100 %, pero el número real puede continuar creciendo. La animación es decorativa y el texto siempre contiene el valor.

## Sol y tiempo exterior

La escala contiene doce soles de cinco minutos. Se puede tocar un sol, arrastrar sobre la fila o escribir minutos exactos (1–720). Los registros del día se guardan localmente y se pueden deshacer. La pantalla llama a la escala «registro» y no la convierte en una dosis sanitaria: no deduce vitamina D ni recomienda exposición.

## Supermercado y tarjetas

Las tarjetas muestran anverso con imagen, marca, macros y energía. «Más nutrientes» gira la tarjeta y presenta nutrientes adicionales por 100 g, ingredientes y alérgenos aportados por la fuente. Cuando no hay información se muestra «Sin datos adicionales»; no se inventan ceros. El botón de información continúa abriendo la ficha completa.

## Nevera

«Abrir nevera» lleva a la biblioteca de favoritos y alimentos usados, que ya está respaldada por Room. La cabecera da una ambientación de nevera sin afirmar que el alimento exista físicamente en casa. La consulta de todos los elementos sigue siendo local y separada de la caché temporal.

## Verificación

P1 añade pruebas de estado para agua sincronizada y local, asociación del identificador remoto, validación del tiempo de sol y navegación instrumentada a Agua y Nevera. El conjunto unitario queda en 49 pruebas sin fallos; la prueba instrumentada de P1 valida los destinos en el emulador API 35.
