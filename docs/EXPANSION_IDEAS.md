# Expansión práctica de ADNApp

La aplicación debe crecer como un sistema de áreas conectadas, pero cada módulo ha de aportar valor por sí solo. La recomendación es lanzar pocas funciones completas, medir uso y ampliar después. Las puntuaciones globales nunca deberían juzgar al usuario: deben explicar qué influyó y ofrecer una acción pequeña y realista.

## Deportes

### Primera versión útil

- **Registro de actividad:** tipo, duración, intensidad percibida, distancia, pasos y notas. Debe admitir tanto entrada manual como sesiones rápidas reutilizables.
- **Plan semanal:** objetivos sencillos (días activos, minutos, pasos o sesiones) y calendario que muestre cumplimiento sin castigar los días de descanso planificado.
- **Biblioteca de rutinas:** fuerza, movilidad, caminar/correr y ejercicios sin material. Cada rutina con duración estimada, dificultad y sustituciones.
- **Progreso:** volumen semanal, constancia, mejores marcas y carga progresiva. Evitar comparar disciplinas incompatibles con una única métrica.
- **Recuperación:** sueño percibido, fatiga, dolor y disposición para entrenar. Si hay fatiga alta, recomendar bajar intensidad o descansar, no dar diagnósticos.

### Ramas del árbol

- **Actividad** → registrar, historial, sesión rápida.
- **Rutinas** → explorar, rutina de hoy, crear rutina.
- **Progreso** → semana/mes, marcas, constancia.
- **Recuperación** → check-in, descanso, movilidad.
- **Objetivos** → días activos, pasos, prueba o evento.

### Conexiones con Nutrición

- Ajustar recomendaciones de agua a duración e intensidad del ejercicio, dejando claro que son estimaciones.
- Mostrar energía y proteína junto a días de entrenamiento, sin “compensar” automáticamente calorías gastadas.
- Relacionar peso, descanso, constancia y rendimiento mediante tendencias; no presentar correlaciones como causas.

### Orden recomendado

1. Registro manual y objetivo semanal.
2. Historial y gráficas de constancia.
3. Rutinas reutilizables y recuperación.
4. Integraciones con Health Connect o wearables sólo si existe demanda real.

## Filosofía y bienestar reflexivo

Este módulo puede diferenciar la app si evita convertirse en una colección de frases. La filosofía debe ayudar a pensar y actuar, no simular terapia ni imponer una escuela.

### Primera versión útil

- **Pregunta del día:** una pregunta breve con respuesta privada y opción de revisarla más adelante.
- **Diario guiado:** plantillas como “qué controlo / qué no controlo”, revisión de decisiones, gratitud concreta o premortem.
- **Principios personales:** el usuario define valores y compromisos; la revisión semanal comprueba si sus acciones estuvieron alineadas.
- **Ideas en contexto:** piezas cortas de estoicismo, epicureísmo, existencialismo, ética de la virtud y pensamiento crítico, citando autor y obra.
- **Decisiones:** herramienta para formular opciones, costes, sesgos, horizonte temporal y criterio de decisión.
- **Rachas saludables:** frecuencia flexible, sin penalizar descansos ni convertir la reflexión en una obligación.

### Ramas del árbol

- **Reflexión** → pregunta diaria, diario libre, revisión.
- **Principios** → valores, compromisos, conflictos.
- **Decisiones** → analizar decisión, historial, resultado posterior.
- **Biblioteca** → escuelas, autores, conceptos guardados.
- **Perspectiva** → sesgos cognitivos, argumentos, contraargumentos.

### Experiencias que conectan áreas

- Tras una semana irregular de nutrición o deporte, ofrecer una reflexión opcional sobre obstáculos, nunca una reprimenda.
- Permitir asociar una decisión a un objetivo financiero, deportivo o de salud y revisarla en una fecha elegida.
- Crear una revisión semanal transversal: qué funcionó, qué drenó energía y cuál será el siguiente paso pequeño.

### Límites de producto

- Diario cifrado o, como mínimo, reglas de acceso y exportación/borrado muy claras.
- Contenido editorial con fuentes; distinguir citas literales de paráfrasis.
- Ante texto que sugiera crisis o autolesión, mostrar recursos de ayuda y no intentar actuar como terapeuta.
- No incluir la filosofía en la puntuación diaria salvo que el usuario lo active y defina qué significa cumplir.

## Finanzas

### Primera versión útil

- **Hucha:** objetivo, fecha, aportaciones y previsión realista.
- **Presupuesto simple:** ingresos y gastos por categorías, con gastos recurrentes.
- **Objetivos:** colchón de emergencia, deuda, compra o viaje; ordenar por prioridad.
- **Patrimonio:** evolución de efectivo, deuda e inversiones introducidas manualmente.
- **Inversiones educativas:** asignación y evolución, con avisos claros de que no constituye asesoramiento financiero.

### Ramas del árbol

- **Hucha** → aportar, metas, previsión.
- **Movimientos** → añadir, recurrentes, categorías.
- **Presupuesto** → mes actual, alertas, revisión.
- **Inversiones** → cartera, asignación, aprendizaje.
- **Objetivos** → deuda, emergencia, metas personales.

## Modelo transversal recomendado

Cada área debería compartir cuatro conceptos: `Goal`, `DailyMetric`, `JournalEntry` y `ProgressSnapshot`. Esto permite construir calendario, gráficas y revisiones comunes sin mezclar los datos específicos. La puntuación del día debería guardar también sus componentes y pesos para poder explicar por qué un día aparece verde, amarillo o rojo incluso si el usuario cambia prioridades posteriormente.

Para privacidad y mantenibilidad, separar colecciones por área (`users/{uid}/areas/{area}`) y ofrecer exportación y borrado por módulo. Las integraciones externas deben ser opcionales y posteriores a una versión manual que ya sea útil.
