/* ---------------------------------------------------------------------
 * Proyecto          : Sistema de Gestión de Riesgos - Prevenía
 * Entidad           : estados_reporte (Gestión de Estados y Ciclo de Vida)
 * Programado por    : [Dayana García]
 * Fecha             : 06/11/2026
 * -------------------------------------------------------
 * HU-06 a HU-10     : Gestión CRUDL de estados del reporte.
 * RF-16 a RF-30     : Requisitos funcionales de registro, consulta, edición, baja y listado de estados.
 * RF-127 a RF-129   : Operación de deshacer mediante Pila propia.
 * RN-04             : Control del ciclo de vida de los reportes.
 * -------------------------------------------------------
 * Criterios: CA-06.1 (bueno), CA-09.1 (falla controlada), CA-43.1 (Pila)
 * Estructuras: Arreglo tradicional con contador y Pila propia con arreglo
 * ------------------------------------------------------------------ */

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

// Clase que representa la estructura de un Estado de Reporte individual
class DatosEstado {
    String idEstado;
    String nombreEstado;
    String descripcion;

    // Constructor para inicializar los atributos del estado
    public DatosEstado(String idEstado, String nombreEstado, String descripcion) {
        this.idEstado = idEstado;
        this.nombreEstado = nombreEstado;
        this.descripcion = descripcion;
    }

    // Convierte el objeto en una línea de texto separada por ';' para guardarlo en archivo plano
    public String convertirACSV() {
        return idEstado + ";" + nombreEstado + ";" + descripcion;
    }
}

// =====================================================================
// ESTRUCTURA DE DATOS: PILA PROPIA (Implementada con arreglo estático)
// Sin usar librerías preconstruidas como Stack ni Collections.
// =====================================================================
class PilaEstado {
    DatosEstado[] datosPila = new DatosEstado[50]; // Arreglo estático para la pila
    int tope = -1; // Indica la posición del último elemento (si es -1, está vacía)

    // Método para apilar (guardar) un elemento respaldado
    void apilar(DatosEstado item) {
        if (tope < datosPila.length - 1) {
            tope++;
            datosPila[tope] = item;
        }
    }

    // Método para desapilar (sacar) el último elemento guardado
    DatosEstado desapilar() {
        if (tope == -1) {
            return null; // La pila está vacía
        }
        DatosEstado item = datosPila[tope];
        datosPila[tope] = null; // Limpia la referencia
        tope--;
        return item;
    }

    // Valida si la pila se encuentra vacía
    boolean estaVacia() {
        return tope == -1;
    }
}

public class EstadosReporte {

    static final int MAX = 50;
    static DatosEstado[] arregloEstados = new DatosEstado[MAX]; // Arreglo maestro principal
    static int contador = 0; // Lleva el control estricto de elementos activos
    static PilaEstado pilaEliminados = new PilaEstado(); // Instancia de la Pila para deshacer
    static String archivoDatos = "datos_estados.txt";

    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);
        int opcion = 0;

        // Carga los datos previos desde el archivo plano al iniciar
        cargarArchivo();

        // Si el archivo está vacío, precarga los estados obligatorios del sistema
        if (contador == 0) {
            arregloEstados[contador] = new DatosEstado("EST-1", "Recibido", "Alerta ingresada por la comunidad.");
            contador++;
            arregloEstados[contador] = new DatosEstado("EST-2", "En revision", "Evaluación técnica en terreno.");
            contador++;
            arregloEstados[contador] = new DatosEstado("EST-3", "Atendido", "Riesgo mitigado o solucionado.");
            contador++;
            arregloEstados[contador] = new DatosEstado("EST-4", "Rechazado", "Alerta desestimada por inviabilidad.");
            contador++;
        }

        do {
            // Menú interactivo en consola
            System.out.println("\n-----------------------------------------------------------");
            System.out.println("  MODULO ADMINISTRATIVO - GESTION DE ESTADOS DE REPORTE");
            System.out.println("-----------------------------------------------------------");
            System.out.println("1. Registrar nuevo estado");
            System.out.println("2. Consultar estado por ID");
            System.out.println("3. Actualizar estado");
            System.out.println("4. Eliminar estado");
            System.out.println("5. Listar todos los estados");
            System.out.println("6. Deshacer ultima eliminacion (Pila)");
            System.out.println("7. Guardar y salir");
            System.out.print("Seleccione una opcion (1-7): ");

            if (entrada.hasNextInt()) {
                opcion = entrada.nextInt();
                entrada.nextLine(); // Limpiar buffer
            } else {
                System.out.println("Por favor ingrese un numero valido.");
                entrada.nextLine();
                continue;
            }

            switch (opcion) {
                case 1:
                    crearEstado(entrada);
                    break;
                case 2:
                    consultarEstado(entrada);
                    break;
                case 3:
                    actualizarEstado(entrada);
                    break;
                case 4:
                    eliminarEstado(entrada);
                    break;
                case 5:
                    listarEstados();
                    break;
                case 6:
                    deshacerEliminacion();
                    break;
                case 7:
                    guardarArchivo();
                    System.out.println("Saliendo del modulo de estados. Datos guardados.");
                    break;
                default:
                    System.out.println("Opcion no valida. Intente de nuevo.");
            }
        } while (opcion != 7);

        entrada.close();
    }

    // C (Create): Registrar un nuevo estado validando capacidad y campos obligatorios
    static void crearEstado(Scanner entrada) {
        if (contador >= MAX) {
            System.out.println("Error: El arreglo de estados esta lleno.");
            return;
        }

        System.out.println("\n--- Registrar Nuevo Estado ---");
        String idEstado = "EST-" + (contador + 1);
        System.out.println("ID generado automaticamente: " + idEstado);

        System.out.print("Ingrese nombre del estado (Ej: En proceso): ");
        String nombre = entrada.nextLine();

        System.out.print("Ingrese descripcion explicativa: ");
        String desc = entrada.nextLine();

        // Valida campos obligatorios vacíos (RF formal)
        if (nombre.isEmpty() || desc.isEmpty()) {
            System.out.println("Error: Todos los campos obligatorios deben completarse.");
            return;
        }

        // Inserta en el arreglo usando el contador como posición y lo incrementa
        arregloEstados[contador] = new DatosEstado(idEstado, nombre, desc);
        contador++;
        System.out.println("¡Estado registrado con exito!");
    }

    // R (Read): Consulta un estado específico recorriendo el arreglo de forma secuencial
    static void consultarEstado(Scanner entrada) {
        System.out.println("\n--- Consultar Estado ---");
        System.out.print("Ingrese ID del estado a buscar (Ej: EST-1): ");
        String buscado = entrada.nextLine();

        boolean encontrado = false;
        for (int i = 0; i < contador; i++) {
            if (arregloEstados[i].idEstado.equalsIgnoreCase(buscado)) {
                System.out.println("\n¡Estado encontrado!");
                mostrarDetalle(arregloEstados[i]);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            System.out.println("No se encontro un estado con ese identificador.");
        }
    }

    // U (Update): Modifica los datos de un estado existente buscando por su ID
    static void actualizarEstado(Scanner entrada) {
        System.out.println("\n--- Actualizar Estado ---");
        System.out.print("Ingrese el ID del estado a modificar: ");
        String id = entrada.nextLine();

        boolean actualizado = false;
        for (int i = 0; i < contador; i++) {
            if (arregloEstados[i].idEstado.equalsIgnoreCase(id)) {
                System.out.print("Nuevo nombre del estado: ");
                arregloEstados[i].nombreEstado = entrada.nextLine();

                System.out.print("Nueva descripcion: ");
                arregloEstados[i].descripcion = entrada.nextLine();

                System.out.println("¡Estado actualizado exitosamente!");
                actualizado = true;
                break;
            }
        }

        if (!actualizado) {
            System.out.println("No se encontro el estado especificado para actualizar.");
        }
    }

    // D (Delete): Elimina un elemento del arreglo, compacta los espacios y lo respalda en la Pila
    static void eliminarEstado(Scanner entrada) {
        System.out.println("\n--- Eliminar Estado ---");
        System.out.print("Ingrese el ID del estado a eliminar: ");
        String id = entrada.nextLine();

        boolean eliminado = false;
        for (int i = 0; i < contador; i++) {
            if (arregloEstados[i].idEstado.equalsIgnoreCase(id)) {
                
                // 1. Respalda el objeto completo en la Pila antes de borrarlo
                pilaEliminados.apilar(arregloEstados[i]); 

                // 2. Desplaza los elementos hacia la izquierda para tapar el espacio vacío (shift)
                for (int j = i; j < contador - 1; j++) {
                    arregloEstados[j] = arregloEstados[j + 1];
                }

                // 3. Limpia la última posición y decrementa el contador de elementos activos
                arregloEstados[contador - 1] = null;
                contador--;
                
                System.out.println("¡Estado eliminado y respaldado en la Pila correctamente!");
                eliminado = true;
                break;
            }
        }

        if (!eliminado) {
            System.out.println("No se encontro el estado que intenta eliminar.");
        }
    }

    // L (List): Muestra todos los elementos activos contenidos en el arreglo hasta el contador
    static void listarEstados() {
        System.out.println("\n--- Listado Maestro de Estados ---");
        if (contador == 0) {
            System.out.println("No hay estados registrados en el sistema.");
            return;
        }

        for (int i = 0; i < contador; i++) {
            System.out.println("Registro [" + (i + 1) + "]");
            mostrarDetalle(arregloEstados[i]);
            System.out.println("-----------------------------------");
        }
        System.out.println("Total de estados activos: " + contador);
    }

    // OPERACIÓN DE PILA: Saca el último elemento eliminado de la Pila y lo reinserta en el arreglo
    static void deshacerEliminacion() {
        System.out.println("\n--- Deshacer Eliminacion (Pila) ---");
        
        if (pilaEliminados.estaVacia()) {
            System.out.println("No hay acciones de eliminacion pendientes por revertir en la Pila.");
            return;
        }

        // Extrae el elemento del tope de la pila
        DatosEstado recuperado = pilaEliminados.desapilar();
        
        // Lo devuelve al final del arreglo activo y aumenta el contador
        arregloEstados[contador] = recuperado;
        contador++;
        
        System.out.println("¡Estado " + recuperado.idEstado + " (" + recuperado.nombreEstado + ") restaurado con exito!");
    }

    // Guardar los datos actuales en un archivo de texto plano al salir
    static void guardarArchivo() {
        try (PrintWriter pw = new PrintWriter(new File(archivoDatos))) {
            for (int i = 0; i < contador; i++) {
                pw.println(arregloEstados[i].convertirACSV());
            }
        } catch (Exception e) {
            System.out.println("Error al guardar los datos de estados en el archivo.");
        }
    }

    // Cargar los datos desde el archivo plano al iniciar la aplicación
    static void cargarArchivo() {
        File f = new File(archivoDatos);
        if (!f.exists()) {
            return; // Si el archivo no existe, no hace nada
        }

        try (Scanner lector = new Scanner(f)) {
            while (lector.hasNext() && contador < MAX) {
                String linea = lector.nextLine();
                String[] partes = linea.split(";"); // Divide la línea por punto y coma
                if (partes.length == 3) {
                    arregloEstados[contador] = new DatosEstado(partes[0], partes[1], partes[2]);
                    contador++;
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudieron cargar los datos del archivo de estados.");
        }
    }

    // Método auxiliar para imprimir los detalles de un estado en consola
    static void mostrarDetalle(DatosEstado e) {
        System.out.println("  ID Estado   : " + e.idEstado);
        System.out.println("  Nombre      : " + e.nombreEstado);
        System.out.println("  Descripcion : " + e.descripcion);
    }
}