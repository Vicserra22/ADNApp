# Componentes compartidos y navegación

## Reutilizar desde ahora

- `CellShape`, `CellSurface` y `FoodIllustration`: contornos celulares y dibujos de los accesos y categorías de Nutrición. El ADN conserva el centro y el eje de alineación del navegador flotante.
- `LocalFloatingNavigationInset`: margen desplazable final para que las listas pasen bajo la navegación sin dejar sus últimos elementos tapados. No aplicarlo como relleno exterior del NavHost, que impediría el efecto solicitado.
- `NutritionHomeScreen`: Súper, Mercadillo y Lo mejor de la casa, con destinos separados y acceso a alimentos guardados. La dirección de diseño se documenta en `P0_DISENO_Y_NAVEGACION.md`.

- `core/ui/NutritionProgressCard`: resumen único para el día actual y los días del calendario; comparte barras, colores, textos y tolerancias, incluida la ausencia de penalización por exceso de proteína.
- `core/ui/NavigationIcons`: iconos Lucide de casa, gráfico, destellos y engranaje, con trazo redondeado y colores del área. Licencia incluida en assets/licenses/lucide.txt.

- `core/ui/AppBottomSheet`: contenedor modal común con altura limitada, scroll, espaciado e insets. Usado por confirmación de fecha, filtros de Agenda, resumen del día y configuración del cambio rápido.
- `core/ui/EntryDateSheet` y `domain/model/EntryDateChoice`: elección explícita de la fecha destino. El formulario conserva su fecha y la entrada queda pendiente hasta confirmar; cancelar no escribe. Los registros existentes se editan en su fecha original.
- `feature/dashboard/ProgressCalendar`: calendario compartido entre Agenda y Nutrición, con navegación por flechas y arrastre horizontal. Mantiene el mes al recrearse. No conoce repositorios ni filtros: recibe puntuaciones y un callback.
- `core/ui/AreaSwitchButton`: toque para selector, pulsación de 1.500 ms para avanzar una sola área, cancelación al soltar antes y respuesta háptica.
- `core/ui/AreaCycleSettings`: selección y orden del ciclo (mínimo dos áreas), guardado local.
- `CompactTopBar`, `AreaIcon` y `AreaTheme`: cabeceras, iconos y colores semánticos comunes.

## Agenda y datos futuros

Agenda presenta el calendario transversal y sus filtros; Nutrición presenta exclusivamente su puntuación. Actualmente solo Nutrición produce datos: los módulos futuros aparecen sin datos y se excluyen de la valoración. Al implementar Deporte, Agenda, Finanzas y Filosofía, introducir un proveedor de resúmenes diarios por área con fecha, puntuación opcional, componentes y estado de disponibilidad. No rellenar ausencias con cero ni simular progreso.

El resumen de Agenda muestra indicadores mejores y a mejorar cuando existen, con acceso al detalle nutricional. El historial de las gráficas excluye el día actual por defecto.

## Navegación y comprobaciones

Cada área conserva su controlador y pila. El contenido de cada host captura un área inmutable; se desactivan las transiciones superpuestas para que Perfil u otra pantalla no se dibujen sobre contenido saliente. Perfil dispone de un regreso explícito.

El orden de pulsación larga por defecto es Nutrición → Deporte → Finanzas → Filosofía → Agenda. Si el área actual no participa en el ciclo personalizado, se entra por la primera seleccionada. Un toque corto permite seguir accediendo a todas las áreas.
