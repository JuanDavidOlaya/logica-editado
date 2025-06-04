package torneo;

import javax.swing.JOptionPane; // Importa para usar ventanas de diálogo para entrada y salida de datos
import java.util.HashSet; // Importa HashSet para almacenar nombres únicos
import java.util.Random; // Importa la clase Random para generar números aleatorios

public class Torneo { // Clase principal que maneja la lógica del torneo completo
    Equipo[] equipos = new Equipo[10]; // Arreglo para almacenar 10 equipos participantes
    Partido[][] partidos = new Partido[10][10]; // Matriz para almacenar partidos entre los equipos

    // Método para registrar los nombres de los equipos usando ventanas de diálogo
    public void registrarEquipos() {
        HashSet<String> nombresEquipos = new HashSet<>(); // Conjunto para verificar nombres únicos
        for (int i = 0; i < 10; i++) { // Repite para cada uno de los 10 equipos
            String nombre;
            do {
                // Pide al usuario que ingrese el nombre del equipo i+1
                nombre = JOptionPane.showInputDialog(null, "Nombre del equipo #" + (i + 1));
            } while (nombre == null || nombre.trim().isEmpty() || !nombresEquipos.add(nombre.trim())); // Repite si no se ingresó nada, solo espacios o nombre ya existe

            String pais;
            do {
                // Pide al usuario que ingrese el país del equipo
                pais = JOptionPane.showInputDialog(null, "País del equipo " + nombre);
            } while (pais == null || pais.trim().isEmpty()); // Repite si no se ingresó nada o solo espacios

            equipos[i] = new Equipo(nombre.trim(), pais.trim()); // Crea un nuevo equipo con el nombre y país ingresados y lo guarda
        }
    }

    // Método que simula todos los partidos de la etapa 1 (todos contra todos)
    public void jugarEtapa1() {
        for (int i = 0; i < 10; i++) { // Para cada equipo i
            for (int j = i + 1; j < 10; j++) { // Para cada equipo j mayor que i (evita repetir partidos)
                Partido p = new Partido(equipos[i], equipos[j]); // Crea un partido entre equipo i y j
                p.jugar(); // Simula el partido generando goles, primer gol, puntos, etc.
                partidos[i][j] = p; // Guarda el partido en la matriz en la posición i,j
                partidos[j][i] = p; // También guarda en j,i para acceso simétrico
            }
        }
    }

    // Método que muestra todos los resultados de la etapa 1 en un diálogo individual por equipo
    public void mostrarMarcadoresEtapa1() {
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("Resultados de " + equipos[i].nombre + ":\n\n");
            for (int j = 0; j < 10; j++) {
                if (i != j) {
                    Partido p = partidos[i][j];
                    int goles1 = (p.equipo1 == equipos[i]) ? p.golesEquipo1 : p.golesEquipo2;
                    int goles2 = (p.equipo1 == equipos[i]) ? p.golesEquipo2 : p.golesEquipo1;
                    String rival = (p.equipo1 == equipos[i]) ? p.equipo2.nombre : p.equipo1.nombre;

                    sb.append(equipos[i].nombre + " " + goles1 + " - " + goles2 + " " + rival);

                    if (!p.primerGolJugador.equals("")) {
                        sb.append(" | Primer gol: " + p.primerGolJugador + " min " + p.primerGolMinuto);
                    }
                    sb.append("\n");
                }
            }
            JOptionPane.showMessageDialog(null, sb.toString(), "Resultados equipo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Método que muestra la tabla de posiciones con puntos, goles y diferencia
    public void mostrarTabla(String titulo) {
        ordenar(); // Ordena los equipos según puntos y criterios de desempate
        StringBuilder tabla = new StringBuilder();

        // Encabezado de la tabla con formato alineado
        tabla.append(String.format("%-20s %-6s %-6s %-6s %-6s\n", "Equipo", "Pts", "GF", "GC", "DG"));
        tabla.append("--------------------------------------------------\n");

        for (int i = 0; i < 10; i++) {
            Equipo e = equipos[i];
            // Agrega cada fila con el nombre y estadísticas del equipo
            tabla.append(String.format("%-20s %-6d %-6d %-6d %-6d\n", e.nombre, e.puntos,
                                       e.golesAFavor, e.golesEnContra, e.diferenciaGoles()));
        }
        // Muestra la tabla formateada en un cuadro de diálogo con el título dado
        JOptionPane.showMessageDialog(null, tabla.toString(), titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    // Método para ordenar el arreglo de equipos según criterios de puntos y goles
    public void ordenar() {
        for (int i = 0; i < 9; i++) {
            for (int j = i + 1; j < 10; j++) {
                // Si el equipo j es mejor que i, intercambia posiciones
                if (!mejorQue(equipos[i], equipos[j])) {
                    Equipo tmp = equipos[i];
                    equipos[i] = equipos[j];
                    equipos[j] = tmp;
                }
            }
        }
    }

    // Método que compara dos equipos para decidir cuál es mejor
    public boolean mejorQue(Equipo a, Equipo b) {
        if (a.puntos != b.puntos) return a.puntos > b.puntos; // Primero compara puntos
        if (a.diferenciaGoles() != b.diferenciaGoles()) return a.diferenciaGoles() > b.diferenciaGoles(); // Luego diferencia de goles
        if (a.golesAFavor != b.golesAFavor) return a.golesAFavor > b.golesAFavor; // Luego goles a favor
        return a.primerGolMinuto < b.primerGolMinuto; // Finalmente, menor minuto del primer gol (como desempate)
    }

    // Método que juega la fase de grupos y final del torneo
    public void jugarFaseGruposYFinal() {
        Equipo[] clasificados = new Equipo[8];
        for (int i = 0; i < 8; i++) clasificados[i] = equipos[i]; // Selecciona los primeros 8 equipos clasificados

        // Define grupos con equipos alternados según la clasificación
        Equipo[] grupoA = {clasificados[1], clasificados[3], clasificados[5], clasificados[7]};
        Equipo[] grupoB = {clasificados[0], clasificados[2], clasificados[4], clasificados[6]};

        mostrarGrupos(grupoA, "GRUPO A"); // Muestra los equipos del grupo A
        mostrarGrupos(grupoB, "GRUPO B"); // Muestra los equipos del grupo B

        Equipo ganadorA = jugarGrupo(grupoA, "GRUPO A"); // Juega la fase de grupos para grupo A y obtiene ganador
        Equipo ganadorB = jugarGrupo(grupoB, "GRUPO B"); // Juega la fase de grupos para grupo B y obtiene ganador

        jugarFinal(ganadorA, ganadorB); // Juega la final entre los ganadores de ambos grupos
    }

    // Método que muestra los equipos que conforman un grupo
    public void mostrarGrupos(Equipo[] grupo, String titulo) {
        StringBuilder sb = new StringBuilder(titulo + ":\n");
        for (Equipo e : grupo) {
            sb.append("- " + e.nombre + "\n"); // Lista los nombres de los equipos del grupo
        }
        JOptionPane.showMessageDialog(null, sb.toString()); // Muestra el listado en un cuadro de diálogo
    }

    // Método que simula todos los partidos dentro de un grupo y retorna el equipo ganador
    public Equipo jugarGrupo(Equipo[] grupo, String nombreGrupo) {
        int[] puntos = new int[4]; // Arreglo para llevar puntos dentro del grupo

        for (int i = 0; i < 4; i++) { // Para cada equipo i
            for (int j = i + 1; j < 4; j++) { // Para cada equipo j mayor que i (evita repetir partidos)
                // Pide al usuario los goles de cada equipo en el partido
                int g1 = pedirNumero("[" + nombreGrupo + "] Goles de " + grupo[i].nombre);
                int g2 = pedirNumero("[" + nombreGrupo + "] Goles de " + grupo[j].nombre);

                // Asigna puntos según resultado
                if (g1 > g2) puntos[i] += 3;
                else if (g2 > g1) puntos[j] += 3;
                else { puntos[i]++; puntos[j]++; }
            }
        }

        // Determina el índice del equipo con más puntos
        int mejor = 0;
        for (int i = 1; i < 4; i++) {
            if (puntos[i] > puntos[mejor]) mejor = i;
        }
        return grupo[mejor]; // Retorna el equipo ganador del grupo
    }

    // Método que simula la final entre dos equipos
    public void jugarFinal(Equipo e1, Equipo e2) {
        int g1 = pedirNumero("FINAL: Goles de " + e1.nombre); // Goles equipo 1
        int g2 = pedirNumero("FINAL: Goles de " + e2.nombre); // Goles equipo 2
        String resultado = "FINAL: " + e1.nombre + " (" + e1.pais + ") " + g1 + " - " + g2 + " " + e2.nombre + " (" + e2.pais + ")\n";

        if (g1 != g2) {
            resultado += "CAMPEÓN: " + (g1 > g2 ? e1.nombre + " (" + e1.pais + ")" : e2.nombre + " (" + e2.pais + ")"); // Si no hay empate, ganador directo
        } else {
            resultado += "Empate. Lanzamiento de penales.\n"; // Si hay empate, se define por penales
            while (true) {
                int fallo1 = pedirNumeroPenal(e1.nombre); // Pide si fallo penal e1
                int fallo2 = pedirNumeroPenal(e2.nombre); // Pide si fallo penal e2
                if (fallo1 != fallo2) {
                    resultado += "CAMPEÓN POR PENALES: " + (fallo1 < fallo2 ? e1.nombre + " (" + e1.pais + ")" : e2.nombre + " (" + e2.pais + ")"); // Quien no fallo gana
                    break;
                }
            }
        }
        JOptionPane.showMessageDialog(null, resultado, "RESULTADO FINAL", JOptionPane.INFORMATION_MESSAGE); // Muestra resultado final
    }

    // Método para pedir un número entero válido al usuario, mostrando un mensaje
    public int pedirNumero(String mensaje) {
        while (true) {
            try {
                String entrada = JOptionPane.showInputDialog(null, mensaje); // Pide dato al usuario
                if (entrada == null) continue; // Si canceló, repite
                int numero = Integer.parseInt(entrada); // Intenta convertir a entero
                if (numero >= 0) return numero; // Si es válido (no negativo), retorna el número
            } catch (Exception e) {} // Si hubo error, ignora y repite
            JOptionPane.showMessageDialog(null, "Por favor ingrese un número válido."); // Mensaje de error
        }
    }

    // Método para pedir si se falló un penal, solo acepta 0 o 1
    public int pedirNumeroPenal(String nombreEquipo) {
        while (true) {
            try {
                String entrada = JOptionPane.showInputDialog(null, nombreEquipo + " falló penal? (1 = sí, 0 = no)"); // Pide dato al usuario
                if (entrada == null) continue; // Si canceló, repite
                int numero = Integer.parseInt(entrada); // Intenta convertir a entero
                if (numero == 0 || numero == 1) return numero; // Solo acepta 0 o 1
            } catch (Exception e) {} // Si hubo error, ignora y repite
            JOptionPane.showMessageDialog(null, "Por favor ingrese 0 (no) o 1 (sí)."); // Mensaje de error
        }
    }
}
