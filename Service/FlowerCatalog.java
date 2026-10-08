package Service;

import Entity.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class FlowerCatalog {
    public static final List<FlowerType> ALL = load();
    private FlowerCatalog() {}
    public static FlowerType find(String id) {
        return ALL.stream().filter(f -> f.id().equals(id)).findFirst().orElse(ALL.get(0));
    }
    private static List<FlowerType> load() {
        try {
            Path local = Path.of("Service", "data", "flowers.csv");
            InputStream stream = Files.isRegularFile(local) ? Files.newInputStream(local)
                : FlowerCatalog.class.getResourceAsStream("/Service/data/flowers.csv");
            if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                List<FlowerType> entries = new ArrayList<>(); Set<String> ids = new HashSet<>();
                for (String line; (line = reader.readLine()) != null;) {
                    line = line.strip(); if (line.isEmpty() || line.startsWith("#") || line.startsWith("id,")) continue;
                    String[] c = line.split(",", -1);
                    if (c.length != 7 || !ids.add(c[0])) throw new IOException("Invalid or duplicate flower row: " + line);
                    int seed = Integer.parseInt(c[3]), sale = Integer.parseInt(c[4]), power = Integer.parseInt(c[5]);
                    if (seed < 0 || sale <= 0 || power <= 0) throw new IOException("Invalid flower values");
                    if(!c[0].matches("[a-z0-9_-]+")||!c[6].matches("#[0-9a-fA-F]{6}"))throw new IOException("Invalid flower id or color");
                    entries.add(new FlowerType(c[0],c[1],c[2],seed,sale,power,c[6]));
                }
                if (!entries.isEmpty()) return List.copyOf(entries);
            }
        } catch (IOException | IllegalArgumentException error) { System.err.println("Using default flowers: " + error.getMessage()); }
        return List.of(new FlowerType("daisy","Cúc trắng","Daisy",8,18,10,"#FAEDB2"),
            new FlowerType("lavender","Oải hương","Lavender",10,22,12,"#A682D4"),
            new FlowerType("rose","Hoa hồng","Rose",12,26,15,"#E77A90"),
            new FlowerType("sunflower","Hướng dương","Sunflower",14,30,18,"#F9C144"));
    }
}
