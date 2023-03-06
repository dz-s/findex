package findex;

import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting the file counter program");

        String rootPath = "rootPath";

        var fileIndexer = new Findex(Collections.emptySet(), rootPath);
        fileIndexer.compute();

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String str = scanner.nextLine();
            System.out.println(fileIndexer.search(Set.of(str.split(" "))));
        }

        System.out.println("Ending the file counter program");
    }

}
