# ADNApp: plan de producto y fases de desarrollo

Fecha: 9 de septiembre de 2026. Estado: alcance de esta etapa fijado por el usuario en P0–P5, con prioridad máxima a la calidad. P6–P9 pausadas; foro aparcado. Orden detallado y calendario pendientes de concretar.

## Alcance vigente: P0–P5

Esta decisión sustituye la propuesta anterior de recorrer todas las fases. El objetivo actual es completar diseño y navegación, nutrición visual, organización, deporte personal, pasos/sueño y finanzas propias. El esfuerzo disponible se concentra en estas áreas.

- **Incluido:** P0, P1, P2, P3, P4 y P5, con los criterios de calidad de la sección 8. P0 está cerrado en `a4d071f`; P1 en `e8cc650`; P2, P3, P4 y P5 se cierran en la rama `ADN-3.0` con sus entregas asociadas.
- **Pausado:** P6 (bancos y mercado), P7 (recetas y lectura externa), P8 (sitios/restaurantes/ocio) y P9 (competiciones y cuotas). Se conservan las ideas como referencia; no se desarrollan pantallas, conectores o servicios de esas fases ahora.
- **Foro:** continúa aparcado.
- **Cierre de calidad:** la antigua P10 se incorpora como revisión transversal y cierre de P0–P5. No se posponen las pruebas ni la integración final por quedar numeradas después de P5.
- **Finanzas en esta entrega:** movimientos manuales, recurrentes, balances, proyecciones, metas, hucha, cartera manual y calculadoras. La importación de extractos queda acotada al formato que se seleccione al detallar P5. Conectar automáticamente Revolut/Trade Republic, cotizaciones y noticias pertenece a las fases pausadas.
- **Conexiones activas entre áreas:** tareas y sesiones deportivas, objetivos de ahorro y vencimientos, historial diario, pasos y sueño. Los recorridos con recetas o buscadores de ocio se conservan como ampliaciones futuras.

Más dedicación significa iterar el diseño y comprobar mejor las funciones elegidas. No amplía automáticamente el catálogo de deportes, dispositivos o funciones. La base se mantiene extensible sin construir por adelantado las fases pausadas.

### Dónde concentrar el trabajo adicional

| Fase | Prioridad de profundidad y acabado |
|---|---|
| P0 | Contrastar fotos, resolver navegación y estados, definir ilustraciones y probar los recorridos principales antes de extenderlos |
| P1 | Coherencia del arte, animaciones y gestos; reverso de tarjetas legible; nevera rápida y completa sin conexión |
| P2 | Organización cotidiana sencilla, recurrencias y recordatorios fiables; proyectos enlazados con registros reales |
| P3 | Registro cómodo durante el ejercicio, recuperación de sesiones, GPS probado y análisis explicables con datos comparables |
| P4 | Sincronización resistente a interrupciones, permisos revocados, fuentes duplicadas y cambios de fecha; pruebas con dispositivo real |
| P5 | Cálculos y previsiones verificables, edición y conciliación, hucha y cartera fieles al dinero registrado |

La estimación anterior de P0–P5 suma **112–197 jornadas**; con la reserva inicial de cierre de calidad, **122–215 jornadas**. Es una referencia de esfuerzo del alcance anterior, no un nuevo plazo comprometido ni un tope al pulido. Se revisará tras concretar referencias, paquetes y ritmo real; no se redistribuye mecánicamente el tiempo de las fases pausadas.

## 1. Criterio de trabajo

Prioridad del usuario: máxima calidad, aunque requiera más tiempo. Cuando el tiempo disponible sea limitado, seleccionar menos funciones completas; mantener el nivel de diseño, fiabilidad y comprobación.

La aplicación combina registros personales, planificación y consulta de información externa. Cada área tiene una ambientación propia y comparte navegación, fechas, edición, objetivos e historial. Una animación nunca debe impedir registrar un dato o leer su valor.

Las tres fotos de referencia ya se han recibido. La dirección de P0 y las decisiones de navegación se recogen en [P0_DISENO_Y_NAVEGACION.md](P0_DISENO_Y_NAVEGACION.md): contornos celulares flotantes, ADN centrado y alineado, y tres accesos de Nutrición. El usuario ha solicitado iniciar P0; el acabado se revisa sobre la implementación.

### Base existente revisada

- Android nativo con Kotlin, Jetpack Compose, Koin, Firebase y Room.
- Navegación con selector central de áreas y pestañas locales. Se estudiará su evolución, no se sustituirá a ciegas.
- Nutrición ya tiene registro, búsqueda e historial, además de gráficas y calendario.
- Room contiene productos, recientes y favoritos. La enciclopedia puede aprovecharlo, pero requiere consultas completas, persistencia apropiada y una interfaz nueva.
- Existe una vista Agenda de análisis vinculada a nutrición. No equivale a una Agenda completa de tareas, eventos y proyectos.
- No se ha encontrado integración con Health Connect o pulseras en las fuentes revisadas.
- Finanzas y deporte necesitan desarrollo funcional nuevo.

## 2. Arquitectura de la experiencia

Propuesta a contrastar con las fotos:

- **Hoy / Agenda:** lo previsto, acciones pendientes y registros realizados.
- **Nutrición:** comida, agua, supermercado y nevera. Recetas y lectura externa quedan pausadas.
- **Deporte y recuperación:** sesiones, ejercicios, marcas, pasos, sueño y luz exterior.
- **Finanzas:** movimientos, previsión, ahorro, patrimonio y cartera manual. Las conexiones externas quedan pausadas.
- **Explorar (futuro, pausado):** sitios, restaurantes, ocio y seguimiento de competiciones.

Explorar puede empezar como una sección secundaria; no necesita convertirse inmediatamente en otra área del selector. El registro de luz exterior puede tener accesos desde Hoy y recuperación. La ubicación final se decide al diseñar la navegación. Filosofía queda fuera de esta ampliación, sin eliminar lo existente.

Dentro de cada área: una entrada clara, una acción principal contextual y acceso consistente al historial. Las funciones menos frecuentes no deben competir con registrar comida, agua, entrenamiento o gasto.

Modelo compartido: objetivos, vínculos a fechas, recordatorios, procedencia de datos y enlaces entre entidades. Los modelos específicos de entrenamiento, nutrición y contabilidad siguen separados; no se fuerza todo dentro de una tabla genérica.

## 3. Estimaciones y fases

Unidad: jornada de 6–8 horas efectivas de una persona con experiencia. Los rangos incluyen diseño de cada función, implementación, pruebas relevantes y pulido. La base visual compartida se cuenta en P0; los dibujos y animaciones específicos, en su fase. No son tiempos de ejecución de Codex ni promesas de entrega.

Supuestos: Android, uso individual como primer destino, un idioma inicial, catálogo acotado de deportes y fuentes, y aprovechamiento del proyecto actual. Publicación comercial amplia, iOS, colaboración multiusuario y proveedores adicionales requieren estimación aparte. La espera de permisos, contratos y respuesta de proveedores no está incluida.

| Fase | Entrega | Dificultad | Jornadas | Dependencias |
|---|---|---|---:|---|
| P0 · En desarrollo | Referencias, mapa de pantallas, navegación, sistema visual y contratos de datos comunes | Alta | 12–20 | Fotos recibidas; revisión de la implementación |
| P1 | Agua, soles, tarjetas de supermercado y nevera local | Media–alta | 18–30 | P0 |
| P2 | Agenda, calendario, tareas, recordatorios, proyectos y objetivos | Alta | 20–35 | P0 |
| P3 | Registro deportivo, rutinas, historial, tendencias y recorridos GPS | Muy alta | 25–45 | P0; P2 para la planificación conectada |
| P4 | Health Connect compartido, pasos y sueño | Alta | 12–22 | P0; modelos de actividad y fechas acordados con P3 |
| P5 | Finanzas manuales completas, hucha, balance, previsiones y cartera manual | Alta | 25–45 | P0; P2 para vencimientos y metas conectados |
| P6 · Pausada | Conexión bancaria acotada y datos de mercado para la cartera | Muy alta e incierta | 15–30 | P5 y viabilidad del proveedor |
| P7 · Pausada | Recetas e información de nutrición con fuentes | Alta | 15–28 | P1; P2 para planificar recetas |
| P8 · Pausada | Sitios, restaurantes y ocio en un buscador de planes | Alta | 12–22 | P0; P2 para agendar y P5 para presupuestar |
| P9 · Pausada | Competiciones, clasificaciones, estadísticas, noticias y comparación de cuotas | Alta–muy alta | 15–30 | P0, fuentes externas y motor de lectura de P7 |
| Cierre P0–P5 · Antigua P10 | Comprobación integral de los módulos elegidos y preparación de entrega | Alta | 10–18 | Integrado en esta etapa |
| Aparcado | Foro de cada sector | Sin estimar | — | No diseñar ni implementar en este alcance |

La numeración agrupa entregas y dependencias, no obliga a desarrollar todo en ese orden. P1, P2 y P5 pueden priorizarse según el valor que busque el usuario. Pasos y sueño no requieren terminar antes todos los análisis deportivos.

Como referencia histórica, sumar todas las fases del plan original daba **179–325 jornadas**. Ese total ya no representa la entrega actual, limitada a P0–P5 y su cierre de calidad. Los rangos de P6–P9 se conservan solo para una posible reactivación posterior. No incluyen mantenimiento continuo ni esperas externas.

### Desglose para seleccionar paquetes

Estos rangos descomponen las fases anteriores; no se suman de nuevo al total.

| Paquete | Jornadas | Complejidad dominante |
|---|---:|---|
| P1 Agua | 4–7 | Ilustración, máscara de llenado, interacción y persistencia |
| P1 Sol / luz exterior | 3–5 | Dos estados gráficos, arrastre accesible y semántica de la escala |
| P1 Supermercado | 6–10 | Tarjetas, reverso, datos nutricionales y rendimiento de listas |
| P1 Nevera | 5–8 | Historial local completo, búsqueda, categorías y ambientación |
| P2 Tareas, calendario y recordatorios | 10–17 | Fechas, repeticiones, excepciones, notificaciones y estados |
| P2 Proyectos y objetivos | 6–10 | Hitos, acciones siguientes y progreso medible |
| P2 Conexiones con otras áreas | 4–8 | Planificado frente a realizado y referencias sin duplicación |
| P3 Registro y plantillas deportivas | 10–16 | Métricas distintas por familia de deporte |
| P3 Historial y análisis | 7–13 | Comparabilidad, calidad de datos y explicación de tendencias |
| P3 GPS y recorridos | 8–16 | Pausa, segundo plano, mapas, batería y pruebas en exterior |
| P4 Base de integración | 6–10 | Permisos, disponibilidad, sincronización y procedencia |
| P4 Pasos | 2–4 | Agregación diaria y fuentes duplicadas |
| P4 Sueño | 4–8 | Sesiones nocturnas, siestas y fases ausentes |
| P5 Movimientos y recurrentes | 8–14 | Contabilidad y conciliación |
| P5 Bizcocho, balanza e hucha | 5–9 | Representación visual fiel a las cantidades |
| P5 Previsiones y objetivos | 6–12 | Escenarios, compromisos y explicación de cálculos |
| P5 Cartera manual y calculadoras | 6–10 | Aportaciones, valoración, comisiones y rentabilidad |
| P6 Banco | 8–16 | Un proveedor viable y una primera cuenta corriente |
| P6 Mercado | 7–14 | Una fuente de precios y noticias; cobertura acotada |
| P7 Recetas | 9–17 | Importación de dos fuentes iniciales y modo cocinar |
| P7 Lectura de nutrición | 6–11 | Fuentes, búsqueda, deduplicación y presentación |
| P9 Competiciones | 8–15 | Una liga inicial, estadísticas y noticias |
| P9 Cuotas | 7–15 | Un proveedor, un deporte y mercados equivalentes |

Máxima calidad no significa incluir todos los deportes, bancos y páginas desde el primer paquete: significa terminar el alcance elegido sin estados simulados ni datos inventados.

## 4. Salud, nutrición y deporte: especificaciones de producto

### Agua: botella ilustrada

Botella protagonista a pantalla casi completa, con volumen actual siempre legible. Al tocar, añade una cantidad configurable —por ejemplo, 250 ml como tamaño de registro, no como recomendación— y sube el nivel con una onda breve. Pulsación larga o botón visible para cantidad exacta. Deshacer inmediato e historial editable.

Mantener el número real aunque se supere la capacidad gráfica; el dibujo no debe recortar lo registrado. Objetivo editable, sin deducir una dosis sanitaria universal. La animación funciona con movimiento reducido y no depende de mantener la pantalla encendida. Estética de ilustración de videojuego acogedor, con arte original y coherente con ADNApp.

### Sol: soles que se colorean

Dos recursos con idéntico tamaño y silueta, ambos con transparencia: contorno y versión coloreada con textura. El relleno parcial mediante máscara permite arrastrar con continuidad sin generar decenas de imágenes.

Propuesta de interfaz: 12 soles de 5 minutos, de 0 a 60 minutos en la escala inicial, con entrada exacta para registrar más. Es una escala de registro, no una dosis recomendada ni un límite médico. Permitir corregir el tiempo; cero significa cero minutos, y sin registro es un estado distinto.

Registrar hora, duración y, opcionalmente, luz exterior en sombra o sol directo. No confundir estar al aire libre con tomar el sol sobre la piel. El máximo gráfico no genera un premio por exposición y no se traduce a vitamina D.

No se establecerá una meta sanitaria basándose en tendencias de comunidades cetogénicas o de usuarios de gafas rojas: no aportan por sí solas una dosis universal válida. La OMS recomienda protección desde un índice UV de 3; la AAD no recomienda obtener vitamina D mediante exposición UV intencionada. Si se incorpora índice UV local, se mostrará con hora y fuente, no como permiso para exponerse. [OMS](https://www.who.int/news-room/fact-sheets/detail/ultraviolet-radiation), [AAD](https://www.aad.org/media/stats-vitamin-d).

### Supermercado: cinta y tarjetas

Cinta de supermercado ilustrada, sin desplazamiento perpetuo. Las tarjetas tienen contorno, sombra discreta y jerarquía clara. Anverso: foto, nombre, marca, formato, cantidad seleccionada y nutrientes principales. Reverso: vitaminas, minerales, electrolitos, ingredientes y alérgenos cuando la fuente los aporte.

La esquina levantada lleva un icono y una etiqueta breve «Más nutrientes». Tocar esa esquina gira la tarjeta; tocar el cuerpo mantiene la acción habitual del producto. Alternativa accesible mediante botón, con el estado de la cara conservado durante la interacción.

Los valores desconocidos aparecen como «Sin datos», nunca como cero. Indicar si corresponden a 100 g, 100 ml o a la ración. Revisar unidades y conversiones antes de mostrar porcentajes. La tarjeta debe seguir siendo usable con nombres largos, sin foto y con muy pocos datos.

### Nevera: enciclopedia personal

Interior de una nevera ilustrada con estantes por categorías, búsqueda alfabética y vista de lista rápida. Cada alimento abre una ficha con información, primera y última utilización, número de usos, favoritos y acceso a sus registros.

Se reutiliza la base Room, pero se añade navegación paginada por todo el historial, no solo la lista actual de 20 recientes. Separar el archivo personal persistente de cualquier caché temporal y conservar referencias aunque cambien los datos del proveedor. Los registros antiguos mantienen la información necesaria para reproducir lo que se contabilizó.

La nevera representa alimentos usados o guardados; no afirma que estén físicamente en casa. Un inventario con cantidades y caducidades sería otro alcance. En recetas, «lo tengo» requiere confirmación del usuario mientras no exista ese inventario.

### Deporte genérico

Carrusel horizontal con ilustraciones originales, nombre del deporte y favoritos. Debe haber una forma de abrir el catálogo completo. El formulario cambia según el deporte:

| Familia | Datos principales | Comparación útil |
|---|---|---|
| Fuerza | Ejercicio, series, repeticiones, peso y descanso | Mismo ejercicio y variante, volumen y marcas |
| Carrera / caminata / ciclismo | Tiempo, distancia, desnivel y recorrido | Ritmo o velocidad en condiciones comparables |
| Natación | Tiempo, distancia, largos y estilo | Ritmo por distancia y estilo |
| Raqueta / equipo | Duración, modalidad, resultado y notas | Evolución dentro de una misma modalidad |
| Movilidad / yoga / otros | Duración, rutina y esfuerzo percibido | Constancia y objetivos definidos por el usuario |

El primer alcance detallado cubre fuerza, caminar/correr y ciclismo; el resto empieza con registro genérico honesto. Rutinas reutilizables, repetir sesión, temporizadores, edición posterior e historial por ejercicio.

Análisis: semana/mes, volumen, constancia, mejores marcas y comparación con la sesión anterior. Un aviso de posible estancamiento debe mostrar ejercicio, métrica, intervalo y número de sesiones comparables, con umbral configurable. Pocos datos producen «Datos insuficientes», no una conclusión. Cambios de técnica, variante o recorrido se distinguen. Sueño y rendimiento pueden mostrarse juntos sin atribuir causalidad.

GPS es un paquete propio: iniciar, pausar, reanudar y terminar; recuperación tras interrupción; trazado, distancia y batería comprobados en un dispositivo real. Importar recorridos existentes y grabarlos en directo son capacidades distintas.

### Pasos y sueño

Una integración con Health Connect alimenta dos experiencias. El usuario concede los permisos correspondientes y ve origen y última actualización. Actualizar al abrir y en tareas periódicas cuando el dispositivo permita lectura en segundo plano. No prometer flujo en tiempo real ni una frecuencia exacta. [Lectura en Health Connect](https://developer.android.com/health-and-fitness/health-connect/read-data).

Pasos: total diario, tendencia semanal y meta editable. Usar agregación y selección de fuentes apropiadas, sin sumar a ciegas teléfono y pulsera. Sueño: sesión principal, siestas, duración y regularidad; fases solo si existen. Definir una convención de fecha visible para noches que cruzan medianoche y respetar zonas horarias.

Desconectar la integración no borra automáticamente el historial ya importado. Definir por separado revocación de acceso y borrado de registros. Si hay registros manuales y automáticos superpuestos, ofrecer una resolución explícita y reproducible.

### Recetas — P7 pausada

Flujo: buscar o pegar enlace → revisar receta → ajustar raciones → comprobar ingredientes → lista de compra → modo cocinar con pasos y temporizadores → registrar ración y, si procede, planificar en Agenda.

Candidatas iniciales en español: Directo al Paladar y PequeRecetas. Las páginas consultadas ofrecen ingredientes e instrucciones; Directo al Paladar también muestra tiempos de preparación en el ejemplo revisado. Esto las hace candidatas para una prueba de importación, no demuestra todavía que sean las más fáciles de integrar o que autoricen su redistribución. [Ejemplo Directo al Paladar](https://www.directoalpaladar.com/recetas-de-carnes-y-aves/pollo-asado-ultrarrapido-mesa-30-minutos/amp), [ejemplo PequeRecetas](https://www.pequerecetas.com/receta/dulce-membrillo-casero/).

Preferir datos estructurados Recipe/JSON-LD cuando existan: ingredientes, raciones, tiempos e instrucciones. No prometer cobertura general antes de probar muestras variadas y revisar las condiciones de cada fuente. [Schema.org Recipe](https://schema.org/Recipe).

Conservar autor y enlace. Reutilizar contenido e imágenes según permisos; si una fuente no lo permite, presentar un enlace o usar otra fuente. Recalcular cantidades con unidades fiables, pedir revisión para expresiones ambiguas y etiquetar los nutrientes calculados como estimaciones. Cambiar ingredientes no garantiza eliminar alérgenos o contaminación cruzada.

### Noticias y curiosidades de nutrición — P7 pausada

Un lector con temas guardados: alimentos, productos, nutrición y alertas. Ficha con título, fecha, fuente, tipo de contenido y enlace; resumen breve cuando sea posible. Diferenciar noticia, estudio, opinión y comunicación comercial. Guardar para leer después y vincular un artículo a un alimento de la nevera.

AESAN es una candidata para información y alertas oficiales; medios y publicaciones adicionales se seleccionarán por tema y condiciones. Priorizar feeds o APIs disponibles, y usar extracción controlada donde sea apropiada. El buscador no recorre toda la web sin límites ni atribuye automáticamente veracidad a lo extraído. [AESAN](https://www.aesan.gob.es/).

## 5. Finanzas: una historia coherente del dinero

Pregunta principal al abrir: **«¿Qué tengo, qué está comprometido y cuánto margen me queda?»** Separar saldo actual, balance del periodo, patrimonio y previsión para que el usuario no tenga que interpretar una cifra ambigua.

| Concepto | Función | Imagen / interacción |
|---|---|---|
| Gastos | Movimientos, categorías y recurrentes: alquiler, luz, suscripciones | Bizcocho por porciones; tocar una abre el detalle |
| Ingresos | Nómina y extras, cobrados o previstos | Entrada al lado de ingresos de la balanza |
| Futuros gastos | Facturas, cuotas de deuda, ocio previsto y compras pendientes | Compromisos sobre el calendario financiero y la balanza |
| Balance | Ingresos menos gastos del periodo | Balanza con importes siempre visibles |
| Ahorros | Reservas y aportaciones asignadas a objetivos | Hucha animada con cantidades verificables |
| Objetivos | Importe, fecha, prioridad y ritmo de aportación | Progreso e hitos de la hucha |
| Proyección | Evolución estimada del efectivo con compromisos y gasto habitual | Línea futura distinguida de los datos reales |
| Análisis | Qué cambió, qué lo explica y siguiente acción | Gráfica seleccionable con desglose |
| Inversiones | Posiciones, aportaciones, comisiones, precios y resultados | Cartera ilustrada con fichas de activos |
| Datos, bolsa y noticias | Contexto de los activos y fecha de valoración | Lecturas asociadas a cada activo |

### Distribución en pantallas

1. **Resumen:** saldo disponible, compromisos próximos y margen previsto.
2. **Movimientos:** ingresos/gastos, búsqueda, etiquetas y recurrentes.
3. **Previsión:** calendario, balanza y escenarios.
4. **Metas:** hucha, colchón y otros objetivos.
5. **Cartera:** patrimonio e inversiones.

Son secciones internas; no exigen cinco botones nuevos en la navegación global. La estética de bizcocho, balanza, hucha y cartera se aplica dentro de un mismo sistema de controles.

### Cómo evitar números engañosos

- Distinguir movimientos reales de los futuros; una factura prevista se concilia con el cargo real y no se cuenta dos veces.
- Una transferencia entre cuentas propias no es ingreso ni gasto. Lo mismo ocurre al mover dinero entre efectivo y una inversión: registrar por separado aportación, comisión y resultado.
- Asignar 100 € a una hucha virtual no ejecuta una transferencia bancaria ni crea otros 100 € de patrimonio.
- La parte de principal pagada de una deuda reduce efectivo y deuda; los intereses son un coste. Mostrar flujo de caja y patrimonio con sus criterios correspondientes.
- Los precios de inversión llevan fecha, moneda y procedencia. Rentabilidad y aportaciones se muestran por separado; no llamar ganancia al dinero que ingresó el usuario.
- En la proyección, separar recurrentes conocidos, gastos variables estimados y extraordinarios. No aplicar la media mensual completa encima de los recurrentes ya incluidos.
- Un gasto previsto de ocio puede reservar presupuesto, pero solo se convierte en gasto real al registrarse o importarse.

Ejemplo ilustrativo de presupuesto mensual: ingresos 2.200 €, fijos 1.100 €, variables previstos 450 €, cuotas de deuda 100 € y ahorro reservado 300 € dejan **250 € de margen presupuestado**. No es automáticamente el saldo de hoy: depende de cuándo se cobran y pagan los movimientos.

### Hucha y análisis profundo

Hucha con monedas que entran al registrar una aportación real o una asignación de reserva claramente identificada. Objetivos con importe, fecha y prioridad; saldo de reservas respaldado por cuentas. Evitar asignar el mismo dinero a dos metas sin indicarlo.

Análisis de una meta: cuánto falta, cuánto se está aportando y fecha estimada si se mantiene ese ritmo. Comparar escenarios editables, sin presentar rendimientos futuros como garantizados. Para el colchón, el usuario fija los meses objetivo y puede elegir qué gastos considera esenciales.

### Cartera manual activa; integraciones de P6 pausadas

Comenzar por cartera manual e importación revisable de extractos: cuentas, posiciones, compras/ventas, aportaciones y comisiones. Añadir después una fuente de precios con cobertura comprobada y una calculadora de aportaciones e interés compuesto con hipótesis visibles.

Revolut y Trade Republic se tratarán como conexiones independientes. Consultar saldo y movimientos no garantiza obtener inversiones. La viabilidad de cuenta corriente, cartera y precios se valida por separado antes de comprometer alcance o precio.

Enable Banking publica uso gratuito bajo sus condiciones para cuentas propias vinculadas de particulares; eso no confirma cobertura de estas cuentas concretas ni gratuidad de una aplicación comercial. Servidor, cotizaciones, noticias y agregación de inversiones pueden tener costes distintos. [Condiciones de Enable Banking](https://enablebanking.com/terms/).

P6 presupone un proveedor viable para una primera cuenta y una fuente de mercado. La sincronización automática de todas las inversiones de Revolut o Trade Republic no se da por resuelta dentro de ese rango. Si solo hay acceso a extractos, se mantiene ese método como resultado definido, sin etiquetarlo como sincronización automática.

## 6. Agenda: una mesa de trabajo ilustrada

Propuesta principal: una mesa con un cuaderno semanal, tarjetas de acciones y carpetas de proyectos. La profundidad, el papel y pequeños objetos aportan personalidad; la información conserva contraste y jerarquía. Las acciones básicas también funcionan mediante botones.

### Conceptos sin duplicaciones

| Elemento | Significado | Ejemplo |
|---|---|---|
| Tarea | Acción que se termina, con fecha opcional | Comprar ingredientes |
| Evento | Bloque con comienzo y duración | Entrenar el martes a las 19:00 |
| Recordatorio | Aviso vinculado a algo | Avisar 20 minutos antes del entrenamiento |
| Hábito | Acción recurrente con frecuencia elegida | Revisar gastos los domingos |
| Objetivo | Resultado medible | Ahorrar 1.200 € antes de una fecha |
| Proyecto | Conjunto de tareas e hitos para un resultado | Preparar un viaje |
| Registro | Hecho sucedido en otra área | Sesión terminada o gasto contabilizado |

Las vistas de calendario, lista y proyecto representan los mismos elementos. Reprogramar una tarea no genera una copia ni modifica el historial de lo ya realizado.

### Cinco funciones que la diferencian

1. **Hoy en una página:** hasta tres prioridades destacadas, citas y siguientes acciones. El resto sigue accesible. Resumen de registros del día por área.
2. **Semana con espacio real:** comparar duración estimada de tareas con huecos libres. Señalar solapamientos y permitir arrastrar; la app no cambia citas sin una acción explícita.
3. **Bolsillo de pendientes:** captura rápida de ideas sin obligar a elegir proyecto, fecha y prioridad. Clasificación posterior en «Hoy», «Esta semana», «Algún día» o proyecto.
4. **Proyectos con siguiente paso:** carpetas ilustradas con hitos, presupuesto opcional y acción siguiente. Un objetivo monetario progresa por dinero y uno de distancia por distancia; el porcentaje de tareas completadas no sustituye esas métricas.
5. **Revisión semanal:** comparar lo planificado y lo hecho, reprogramar lo pendiente y elegir el siguiente paso. Las semanas sin registros no reciben una valoración inventada.

### Un flujo conectado

Proyecto «Preparar una carrera» → sesiones en Agenda → registro de entrenamiento en Deporte → progreso del objetivo. Comprar zapatillas puede añadirse como gasto previsto y conciliase después con el movimiento real.

Proyecto «Escapada» → objetivo de ahorro en Finanzas → sitios guardados en Explorar → reserva introducida como evento → gastos reales vinculados. La hucha puede financiar el viaje sin duplicar sus fondos en patrimonio.

Receta guardada → comida planificada → tarea de compra → modo cocinar → ración registrada. Programar cocinar no añade automáticamente comida consumida.

### Recordatorios de calidad

Incluir recurrencias con excepciones, aplazar, completar desde aviso cuando proceda y horas tranquilas. Comprobar permisos denegados, reinicio, cambio de zona horaria, horario de verano y restricciones de batería. Las tareas periódicas de sincronización y los avisos a una hora concreta necesitan mecanismos adecuados a cada caso.

Calendarios externos, lenguaje natural avanzado y colaboración compartida quedan como ampliaciones separadas, sin inflar el primer alcance. Exportación, recuperación y funcionamiento local de los elementos propios forman parte de la calidad básica.

## 7. Explorar y seguimiento deportivo — P8 y P9 pausadas

### Sitios, restaurantes y ocio: un solo recorrido

Entrada «¿Qué hacemos?» con tipo de plan, zona, fecha, presupuesto y duración. Lista y mapa, fichas con procedencia y fecha de consulta, favoritos y propuesta de horario. Un sitio guardado puede pasar a Agenda y tener presupuesto en Finanzas.

El primer alcance cubre búsqueda de lugares y fichas, más un catálogo acotado de ocio; no presupone un inventario completo y en tiempo real de todos los eventos. Proveedor, cobertura, atribución y coste se validan antes. Reservar, comprar entradas y disponibilidad instantánea son capacidades adicionales.

### Competiciones y cuotas

Separar «Mi entrenamiento» de «Seguir competiciones». En seguimiento: equipos/ligas favoritos, próximos partidos, resultados, clasificación, estadísticas y noticias. Una primera liga permite terminar bien normalización, cambios de horario y temporadas antes de ampliar.

Cuotas como comparador informativo dentro del evento. Comparar el mismo mercado, selección, línea y reglas; mostrar casa, formato y hora de actualización. Una cuota antigua no se presenta como vigente. Usar un proveedor de datos con cobertura y derechos de uso comprobados, como candidato The Odds API, en vez de asumir que todas las casas admiten extracción estable. [Proveedor](https://the-odds-api.com/).

Las cuotas no se incorporan a rentabilidades de inversión ni a previsiones de ingresos. No se incluye ejecutar apuestas. La consulta tiene prioridad posterior a los módulos personales por su dependencia externa y mantenimiento.

## 8. Qué significa terminar con máxima calidad

Cada paquete se cierra cuando cumple sus criterios, no cuando aparece su pantalla:

- Recorrido completo: crear o importar, consultar, editar, deshacer cuando tenga sentido y eliminar.
- Datos persistentes y aislamiento entre usuarios; migraciones comprobadas cuando cambie el almacenamiento.
- Estados vacíos, errores, datos parciales, desconexión y reintento comprensibles.
- Cálculos, unidades, fechas, duplicados y conciliación cubiertos con pruebas relevantes.
- Revisión visual en pantalla pequeña y grande, textos largos, tema claro/oscuro según el diseño y tamaño de texto aumentado.
- Contraste, etiquetas accesibles y alternativa a gestos; movimiento reducido.
- Animaciones y listas fluidas, sin consumo continuo innecesario; medición en dispositivo antes de cerrar las partes críticas.
- Para pulsera, GPS y avisos: validación real en dispositivo, incluida app en segundo plano. No basta con datos simulados.
- Para fuentes externas: atribución, última actualización, ausencia de datos y coste operativo identificados.
- Exportación/borrado de datos personales, permisos y acceso del backend adecuados al módulo.
- Revisión final de navegación entre áreas. No duplicar movimientos ni dar por cumplido un objetivo por el simple hecho de haberlo planificado.

La revisión antes denominada P10 cubre recorridos de conjunto, rendimiento entre módulos y entrega de P0–P5. Se mantiene dentro del alcance vigente y no sustituye las pruebas dentro de cada fase.

## 9. Preparación de la decisión de orden

Orden de referencia de la etapa vigente: **P0 → P1 → P2 → P3/P4 → P5 → cierre de calidad**. P3 y P4 coordinan sus modelos; no obliga a ejecutar ambas en paralelo. Un paquete puede adelantarse si se conservan sus dependencias. P6–P9 no se reactivan por terminar P5: requieren una decisión posterior del usuario. El foro permanece aparcado.

Para calcular qué cabe en un plazo necesitamos: fecha objetivo, horas semanales efectivas, fotos, prioridad entre salud/finanzas/organización, modelo de pulsera y si el primer destino será solo el usuario o una aplicación para terceros.

Regla para la siguiente planificación: seleccionar paquetes completos, añadir sus dependencias y comprobación integral proporcional, y separar esfuerzo de esperas de proveedor. La capacidad calculada con horas disponibles no será una promesa hasta contrastarla con el ritmo real del primer paquete.
