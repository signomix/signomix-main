/*
Sposób wywołania:

java FileModifier.java plik_do_zmiany plik_ze_wstawką tag

np:

java src/main/java/FileModifier.java www/components/app_header.tag www/components/options.html '<!-- additional.options -->'
*/
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileModifier {

    public static void main(String[] args) {
        String include = readFile(args[1], "", "");
        String output = readFile(args[0], args[2], include);
        writeFile(args[0], output);
    }

    public static String readFile(String filename, String tag, String stringToInclude) {
        try {
            StringBuffer sb = new StringBuffer();
            File myObj = new File(filename);
            File output = new File(filename + "~");
            Scanner myReader = new Scanner(myObj);
            String line;
            while (myReader.hasNextLine()) {
                line=myReader.nextLine();
                if(line.trim().equals(tag)){
                    line=stringToInclude;
                }
                sb.append(line).append("\r\n");
            }
            myReader.close();
            return sb.toString();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    public static void writeFile(String name, String output) {
        try {
            String tmpName=name + ".tmp";
            File tmpFile = new File(tmpName);
            if (tmpFile.createNewFile()) {
                System.out.println("File created: " + tmpFile.getName());
            } else {
                System.out.println("File already exists.");
            }
            FileWriter myWriter = new FileWriter(tmpName);
            myWriter.write(output);
            myWriter.close();
            File oldFile=new File(name);
            oldFile.delete();
            tmpFile.renameTo(new File(name));
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
    /*
    <!-- additional.options -->
     */
 /*
    <a class="p-2 text-dark" href="/blog">Blog</a>
    <a class="p-2 text-dark" href="/status/">Status</a>
     */

}
