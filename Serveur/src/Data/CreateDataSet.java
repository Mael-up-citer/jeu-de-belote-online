package src.Data;

import java.io.FileWriter;
import java.io.IOException;

public class CreateDataSet {

    // Déclaration du FileWriter et du chemin de fichier
    private static FileWriter fileWriter;
    private static String filepath = "Data.csv";

    // Bloc static pour initialiser le FileWriter
    static {
        try {
            fileWriter = new FileWriter(filepath, true); // true pour ajouter à la fin du fichier
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour écrire les données dans le fichier
    public static void writeData(String data) {
        try {
            fileWriter.write(data + "\n"); // Ajoute les données et une nouvelle ligne
            fileWriter.flush();  // Force l'écriture sur le disque
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour fermer le FileWriter
    public static void closeWriter() {
        try {
            if (fileWriter != null) {
                fileWriter.close(); // Ferme le fichier
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}