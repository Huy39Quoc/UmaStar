import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * T\u1ef1 \u0111\u1ed9ng qu\u00e9t to\u00e0n b\u1ed9 th\u01b0 m\u1ee5c/file trong Backend (src/main/java, src/main/resources)
 * v\u00e0 c\u1eadp nh\u1eadt kh\u1ed1i AUTO-GENERATED trong PROJECT_SPEC.md, ENTITY_SCHEMA.md & BACKEND_ROADMAP.md.
 *
 * Ch\u1ea1y: java scripts/SyncDocs.java
 */
public class SyncDocs {

    private static final Path ROOT = Paths.get("").toAbsolutePath();
    private static final Path JAVA_ROOT = ROOT.resolve("src/main/java/umastar");
    private static final Path RESOURCES_ROOT = ROOT.resolve("src/main/resources");
    private static final Path PROJECT_SPEC = ROOT.resolve("PROJECT_SPEC.md");
    private static final Path ENTITY_SCHEMA = ROOT.resolve("ENTITY_SCHEMA.md");
    private static final Path BACKEND_ROADMAP = ROOT.resolve("BACKEND_ROADMAP.md");

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern TYPE_DECL_PATTERN = Pattern.compile(
            "(?m)^(?:public\\s+|protected\\s+|abstract\\s+|final\\s+|sealed\\s+|non-sealed\\s+)*" +
                    "(class|interface|enum|record)\\s+(\\w+)");
    private static final Pattern TABLE_PATTERN = Pattern.compile("@Table\\s*\\(.*?name\\s*=\\s*\"([^\"]+)\"", Pattern.DOTALL);
    private static final Pattern FIELD_PATTERN = Pattern.compile("^private\\s+([A-Za-z0-9_<>,\\s?\\[\\]]+?)\\s+([a-zA-Z0-9_]+)\\s*(=.*)?;$");
    private static final Pattern REQUEST_MAPPING_PATTERN = Pattern.compile("@RequestMapping\\s*\\(([^)]*)\\)");
    private static final Pattern MAPPING_PATTERN = Pattern.compile(
            "@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping|RequestMapping)\\s*(?:\\(([^)]*)\\))?");
    private static final Pattern OPERATION_PATTERN = Pattern.compile("@Operation\\s*\\([^)]*summary\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern QUOTED_STRING = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern PUBLIC_METHOD_PATTERN = Pattern.compile(
            "^public\\s+(?:static\\s+)?([\\w<>\\[\\],.?\\s]+?)\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern CHECKBOX_PATTERN = Pattern.compile("(?m)^\\|\\s*\\[([ xX])]\\s*\\|");

    public static void main(String[] args) throws IOException {
        if (!Files.exists(JAVA_ROOT)) {
            System.err.println("Kh\u00f4ng t\u00ecm th\u1ea5y th\u01b0 m\u1ee5c ngu\u1ed3n: " + JAVA_ROOT);
            System.exit(1);
        }

        List<JavaFileInfo> allJava = scanAllJavaFiles();
        syncProjectSpec(allJava);
        syncEntitySchema(allJava);
        syncBackendRoadmap(allJava);
    }

    private static List<JavaFileInfo> scanAllJavaFiles() throws IOException {
        List<JavaFileInfo> list = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(JAVA_ROOT)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .toList();
            for (Path file : files) {
                list.add(parseJavaFile(file));
            }
        }
        return list;
    }

    private static JavaFileInfo parseJavaFile(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        List<String> lines = Arrays.asList(content.split("\\R"));

        String pkg = "";
        Matcher pm = PACKAGE_PATTERN.matcher(content);
        if (pm.find()) {
            pkg = pm.group(1);
        }

        String kind = "class";
        String name = file.getFileName().toString().replace(".java", "");
        Matcher tm = TYPE_DECL_PATTERN.matcher(content);
        if (tm.find()) {
            kind = tm.group(1);
            name = tm.group(2);
        }

        String relativePath = ROOT.relativize(file).toString().replace("\\", "/");
        String classJavadoc = extractClassJavadoc(lines);
        List<String> classAnnotations = extractClassAnnotations(lines);

        return new JavaFileInfo(file, relativePath, pkg, kind, name, classJavadoc, classAnnotations, content, lines);
    }

    private static void syncProjectSpec(List<JavaFileInfo> allJava) throws IOException {
        if (!Files.exists(PROJECT_SPEC)) return;

        StringBuilder sb = new StringBuilder();
        sb.append("> Ph\u1ea7n n\u00e0y \u0111\u01b0\u1ee3c t\u1ef1 \u0111\u1ed9ng sinh t\u1eeb to\u00e0n b\u1ed9 m\u00e3 ngu\u1ed3n trong `src/main/java/umastar/**` v\u00e0 `src/main/resources/**` b\u1eb1ng `java scripts/SyncDocs.java`.\n\n");

        sb.append("### Ki\u1ebfn tr\u00fac Package & File hi\u1ec7n c\u00f3\n\n");
        Map<String, List<JavaFileInfo>> byPackage = allJava.stream()
                .collect(Collectors.groupingBy(f -> f.pkg, TreeMap::new, Collectors.toList()));

        sb.append("| Package | Lo\u1ea1i | T\u00ean | M\u00f4 t\u1ea3 |\n");
        sb.append("|---|---|---|---|\n");
        for (Map.Entry<String, List<JavaFileInfo>> entry : byPackage.entrySet()) {
            for (JavaFileInfo info : entry.getValue()) {
                String desc = info.classJavadoc.isEmpty() ? summarizeAnnotations(info.classAnnotations) : info.classJavadoc;
                sb.append("| `").append(entry.getKey()).append("` | `").append(info.kind).append("` | [`")
                        .append(info.name).append("`](").append(info.relativePath).append(") | ")
                        .append(escapePipe(desc)).append(" |\n");
            }
        }
        sb.append("\n");

        List<JavaFileInfo> controllers = findControllers(allJava);
        sb.append("### API Endpoints (Controllers)\n\n");
        if (controllers.isEmpty()) {
            sb.append("_Ch\u01b0a c\u00f3 Controller n\u00e0o trong m\u00e3 ngu\u1ed3n._\n\n");
        } else {
            sb.append("| Controller | Method | Path | Handler | M\u00f4 t\u1ea3 |\n");
            sb.append("|---|---|---|---|---|\n");
            for (JavaFileInfo ctrl : controllers) {
                for (EndpointInfo ep : extractEndpoints(ctrl)) {
                    sb.append("| `").append(ctrl.name).append("` | `").append(ep.httpMethod).append("` | `")
                            .append(ep.path).append("` | `").append(ep.handler).append("` | ")
                            .append(escapePipe(ep.summary)).append(" |\n");
                }
            }
            sb.append("\n");
        }

        List<JavaFileInfo> services = findServices(allJava);
        sb.append("### Business Services\n\n");
        if (services.isEmpty()) {
            sb.append("_Ch\u01b0a c\u00f3 Service n\u00e0o trong m\u00e3 ngu\u1ed3n._\n\n");
        } else {
            for (JavaFileInfo svc : services) {
                sb.append("- **`").append(svc.name).append("`** (`").append(svc.relativePath).append("`)");
                if (!svc.classJavadoc.isEmpty()) sb.append(": ").append(svc.classJavadoc);
                sb.append("\n");
                for (MethodInfo m : extractPublicMethods(svc.lines)) {
                    sb.append("  - `").append(m.name).append("(").append(m.params).append(")` -> `").append(m.returnType).append("`");
                    if (!m.javadoc.isEmpty()) sb.append(": ").append(m.javadoc);
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }

        List<JavaFileInfo> repos = findRepositories(allJava);
        sb.append("### Repositories\n\n");
        if (repos.isEmpty()) {
            sb.append("_Ch\u01b0a c\u00f3 Repository n\u00e0o trong m\u00e3 ngu\u1ed3n._\n\n");
        } else {
            for (JavaFileInfo repo : repos) {
                sb.append("- **`").append(repo.name).append("`** (`").append(repo.relativePath).append("`)\n");
            }
            sb.append("\n");
        }

        sb.append("### Resources & C\u1ea5u h\u00ecnh\n\n");
        if (Files.exists(RESOURCES_ROOT)) {
            try (Stream<Path> stream = Files.walk(RESOURCES_ROOT)) {
                List<Path> resFiles = stream.filter(Files::isRegularFile).sorted().toList();
                for (Path rf : resFiles) {
                    String rel = ROOT.relativize(rf).toString().replace("\\", "/");
                    sb.append("- `").append(rel).append("`\n");
                }
            }
        }

        replaceMarkerBlock(PROJECT_SPEC, "<!-- AUTO-GENERATED-BACKEND:START -->", "<!-- AUTO-GENERATED-BACKEND:END -->", sb.toString().trim());
    }

    private static void syncEntitySchema(List<JavaFileInfo> allJava) throws IOException {
        if (!Files.exists(ENTITY_SCHEMA)) return;

        List<JavaFileInfo> entities = allJava.stream()
                .filter(f -> f.hasAnnotation("@Entity") || f.hasAnnotation("@MappedSuperclass"))
                .sorted(Comparator.comparing(f -> f.name))
                .toList();
        List<JavaFileInfo> enums = allJava.stream()
                .filter(f -> "enum".equals(f.kind))
                .sorted(Comparator.comparing(f -> f.name))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("C\u1eadp nh\u1eadt t\u1eeb to\u00e0n b\u1ed9 m\u00e3 ngu\u1ed3n: `src/main/java/umastar/**` (`")
                .append(entities.size()).append("` entities/superclasses, `")
                .append(enums.size()).append("` enums).\n\n");

        sb.append("### Danh s\u00e1ch Entity\n\n");
        for (JavaFileInfo entity : entities) {
            String tableName = "(MappedSuperclass)";
            Matcher tm = TABLE_PATTERN.matcher(entity.content);
            if (tm.find()) {
                tableName = "`" + tm.group(1) + "`";
            }
            sb.append("#### `").append(entity.name).append("` \u2014 B\u1ea3ng: ").append(tableName).append("\n");
            sb.append("| Field | Ki\u1ec3u | Ghi ch\u00fa / Annotation |\n");
            sb.append("|---|---|---|\n");

            for (FieldInfo fi : extractFields(entity.lines)) {
                String note = fi.comment.isEmpty() ? fi.annotations : (fi.annotations.isEmpty() ? fi.comment : fi.comment + " \u2014 " + fi.annotations);
                if (note.isEmpty()) note = "-";
                sb.append("| `").append(fi.name).append("` | `").append(fi.type).append("` | ").append(escapePipe(note)).append(" |\n");
            }
            sb.append("\n");
        }

        sb.append("### Danh s\u00e1ch Enum\n\n");
        sb.append("| Enum | Gi\u00e1 tr\u1ecb |\n");
        sb.append("|---|---|\n");
        for (JavaFileInfo en : enums) {
            List<String> values = extractEnumValues(en.content);
            sb.append("| `").append(en.name).append("` | ")
                    .append(values.stream().map(v -> "`" + v + "`").collect(Collectors.joining(", ")))
                    .append(" |\n");
        }

        replaceMarkerBlock(ENTITY_SCHEMA, "<!-- AUTO-GENERATED:START -->", "<!-- AUTO-GENERATED:END -->", sb.toString().trim());
    }

    private static void syncBackendRoadmap(List<JavaFileInfo> allJava) throws IOException {
        if (!Files.exists(BACKEND_ROADMAP)) return;

        String raw = Files.readString(BACKEND_ROADMAP, StandardCharsets.UTF_8);
        Matcher cm = CHECKBOX_PATTERN.matcher(raw);
        int done = 0;
        int total = 0;
        while (cm.find()) {
            total++;
            if ("x".equalsIgnoreCase(cm.group(1))) {
                done++;
            }
        }
        int percent = total == 0 ? 0 : (int) Math.round(done * 100.0 / total);

        List<JavaFileInfo> controllers = findControllers(allJava);
        List<JavaFileInfo> services = findServices(allJava);
        List<JavaFileInfo> repos = findRepositories(allJava);
        int endpointCount = controllers.stream().mapToInt(c -> extractEndpoints(c).size()).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("> T\u1ef1 \u0111\u1ed9ng c\u1eadp nh\u1eadt b\u1edfi `java scripts/SyncDocs.java`.\n\n");
        sb.append("- **Ti\u1ebfn \u0111\u1ed9 checklist:** `").append(done).append("/").append(total).append("` h\u1ea1ng m\u1ee5c (").append(percent).append("%)\n");
        sb.append("- **Th\u00e0nh ph\u1ea7n \u0111\u00e3 c\u00f3 trong code:** `")
                .append(controllers.size()).append("` Controllers (`").append(endpointCount).append("` endpoints), `")
                .append(services.size()).append("` Services, `")
                .append(repos.size()).append("` Repositories\n\n");

        if (!controllers.isEmpty()) {
            sb.append("### Endpoints \u0111\u00e3 tri\u1ec3n khai\n\n");
            sb.append("| Controller | Method | Path | Handler |\n");
            sb.append("|---|---|---|---|\n");
            for (JavaFileInfo ctrl : controllers) {
                for (EndpointInfo ep : extractEndpoints(ctrl)) {
                    sb.append("| `").append(ctrl.name).append("` | `").append(ep.httpMethod).append("` | `")
                            .append(ep.path).append("` | `").append(ep.handler).append("` |\n");
                }
            }
        }

        replaceMarkerBlock(BACKEND_ROADMAP, "<!-- AUTO-GENERATED-PROGRESS:START -->", "<!-- AUTO-GENERATED-PROGRESS:END -->", sb.toString().trim());
    }

    private static List<JavaFileInfo> findControllers(List<JavaFileInfo> allJava) {
        return allJava.stream()
                .filter(f -> f.hasAnnotation("@RestController") || f.hasAnnotation("@Controller"))
                .toList();
    }

    private static List<JavaFileInfo> findServices(List<JavaFileInfo> allJava) {
        return allJava.stream()
                .filter(f -> f.hasAnnotation("@Service") || (f.pkg.contains(".service") && !"interface".equals(f.kind)))
                .toList();
    }

    private static List<JavaFileInfo> findRepositories(List<JavaFileInfo> allJava) {
        return allJava.stream()
                .filter(f -> f.pkg.contains(".repository") || f.content.contains("Repository<"))
                .toList();
    }

    private static void replaceMarkerBlock(Path mdFile, String startMarker, String endMarker, String newBody) throws IOException {
        String existing = Files.readString(mdFile, StandardCharsets.UTF_8);
        int startIdx = existing.indexOf(startMarker);
        int endIdx = existing.indexOf(endMarker);
        if (startIdx < 0 || endIdx < 0 || endIdx < startIdx) {
            System.err.println("Kh\u00f4ng t\u00ecm th\u1ea5y marker h\u1ee3p l\u1ec7 trong " + mdFile.getFileName());
            return;
        }
        String updated = existing.substring(0, startIdx + startMarker.length())
                + "\n" + newBody + "\n"
                + existing.substring(endIdx);

        if (!existing.equals(updated)) {
            Files.writeString(mdFile, updated, StandardCharsets.UTF_8);
            System.out.println("\u0110\u00e3 c\u1eadp nh\u1eadt: " + ROOT.relativize(mdFile));
        } else {
            System.out.println("Kh\u00f4ng c\u00f3 thay \u0111\u1ed5i: " + ROOT.relativize(mdFile));
        }
    }

    private static String extractClassJavadoc(List<String> lines) {
        StringBuilder doc = new StringBuilder();
        boolean inJavadoc = false;
        String pending = "";
        for (String raw : lines) {
            String line = raw.trim();
            if (line.startsWith("/**")) {
                inJavadoc = true;
                doc.setLength(0);
                String rest = line.substring(3);
                if (rest.endsWith("*/")) {
                    doc.append(rest, 0, rest.length() - 2);
                    inJavadoc = false;
                    pending = cleanJavadoc(doc.toString());
                } else {
                    doc.append(rest);
                }
            } else if (inJavadoc) {
                if (line.endsWith("*/")) {
                    doc.append(" ").append(line, 0, line.length() - 2);
                    inJavadoc = false;
                    pending = cleanJavadoc(doc.toString());
                } else {
                    doc.append(" ").append(line.replaceFirst("^\\*\\s?", ""));
                }
            } else if (TYPE_DECL_PATTERN.matcher(line).find()) {
                return pending;
            } else if (!line.isEmpty() && !line.startsWith("@") && !line.startsWith("package ") && !line.startsWith("import ")) {
                pending = "";
            }
        }
        return "";
    }

    private static List<String> extractClassAnnotations(List<String> lines) {
        List<String> anns = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (TYPE_DECL_PATTERN.matcher(line).find()) break;
            if (line.startsWith("@")) {
                anns.add(line.split("\\(")[0]);
            }
        }
        return anns;
    }

    private static List<FieldInfo> extractFields(List<String> lines) {
        List<FieldInfo> fields = new ArrayList<>();
        StringBuilder doc = new StringBuilder();
        boolean inJavadoc = false;
        String pendingComment = "";
        List<String> pendingAnns = new ArrayList<>();
        boolean insideType = false;

        for (String raw : lines) {
            String line = raw.trim();
            if (!insideType) {
                if (TYPE_DECL_PATTERN.matcher(line).find()) {
                    insideType = true;
                }
                continue;
            }
            if (line.startsWith("/**")) {
                inJavadoc = true;
                doc.setLength(0);
                String rest = line.substring(3);
                if (rest.endsWith("*/")) {
                    pendingComment = cleanJavadoc(rest.substring(0, rest.length() - 2));
                    inJavadoc = false;
                } else {
                    doc.append(rest);
                }
                continue;
            }
            if (inJavadoc) {
                if (line.endsWith("*/")) {
                    doc.append(" ").append(line, 0, line.length() - 2);
                    pendingComment = cleanJavadoc(doc.toString());
                    inJavadoc = false;
                } else {
                    doc.append(" ").append(line.replaceFirst("^\\*\\s?", ""));
                }
                continue;
            }
            if (line.startsWith("@")) {
                pendingAnns.add(line);
                continue;
            }
            Matcher fm = FIELD_PATTERN.matcher(line);
            if (fm.matches()) {
                String type = fm.group(1).replaceAll("\\s+", " ").trim();
                String name = fm.group(2);
                String annSummary = String.join(" ", pendingAnns);
                if (annSummary.length() > 90) annSummary = annSummary.substring(0, 87) + "...";
                fields.add(new FieldInfo(name, type, pendingComment, annSummary));
                pendingComment = "";
                pendingAnns.clear();
            } else if (!line.isEmpty()) {
                pendingComment = "";
                pendingAnns.clear();
            }
        }
        return fields;
    }

    private static List<EndpointInfo> extractEndpoints(JavaFileInfo ctrl) {
        List<EndpointInfo> endpoints = new ArrayList<>();
        String basePath = "";
        int classLineIdx = 0;
        for (int i = 0; i < ctrl.lines.size(); i++) {
            String l = ctrl.lines.get(i).trim();
            if (TYPE_DECL_PATTERN.matcher(l).find()) {
                classLineIdx = i;
                break;
            }
            Matcher rm = REQUEST_MAPPING_PATTERN.matcher(l);
            if (rm.find()) {
                basePath = firstQuoted(rm.group(1));
            }
        }

        StringBuilder pendingBlock = new StringBuilder();
        String pendingDoc = "";
        boolean inJavadoc = false;
        StringBuilder docBuf = new StringBuilder();

        for (int i = classLineIdx + 1; i < ctrl.lines.size(); i++) {
            String line = ctrl.lines.get(i).trim();
            if (line.startsWith("/**")) {
                inJavadoc = true;
                docBuf.setLength(0);
                String rest = line.substring(3);
                if (rest.endsWith("*/")) {
                    pendingDoc = cleanJavadoc(rest.substring(0, rest.length() - 2));
                    inJavadoc = false;
                } else {
                    docBuf.append(rest);
                }
                continue;
            }
            if (inJavadoc) {
                if (line.endsWith("*/")) {
                    docBuf.append(" ").append(line, 0, line.length() - 2);
                    pendingDoc = cleanJavadoc(docBuf.toString());
                    inJavadoc = false;
                } else {
                    docBuf.append(" ").append(line.replaceFirst("^\\*\\s?", ""));
                }
                continue;
            }
            if (line.startsWith("@")) {
                pendingBlock.append(line).append(" ");
                continue;
            }
            Matcher mm = PUBLIC_METHOD_PATTERN.matcher(line);
            if (mm.find() && !pendingBlock.isEmpty()) {
                String block = pendingBlock.toString();
                Matcher mapM = MAPPING_PATTERN.matcher(block);
                if (mapM.find()) {
                    String httpMethod = mapM.group(1).replace("Mapping", "").toUpperCase();
                    if ("REQUEST".equals(httpMethod)) httpMethod = "REQUEST";
                    String subPath = mapM.group(2) == null ? "" : firstQuoted(mapM.group(2));
                    String fullPath = normalizePath(basePath, subPath);
                    String summary = pendingDoc;
                    Matcher opM = OPERATION_PATTERN.matcher(block);
                    if (opM.find()) summary = opM.group(1);
                    if (summary.isEmpty()) summary = "-";
                    endpoints.add(new EndpointInfo(httpMethod, fullPath, mm.group(2) + "()", summary));
                }
                pendingBlock.setLength(0);
                pendingDoc = "";
            } else if (!line.isEmpty()) {
                pendingBlock.setLength(0);
                pendingDoc = "";
            }
        }
        return endpoints;
    }

    private static List<MethodInfo> extractPublicMethods(List<String> lines) {
        List<MethodInfo> methods = new ArrayList<>();
        boolean insideType = false;
        boolean inJavadoc = false;
        StringBuilder docBuf = new StringBuilder();
        String pendingDoc = "";

        for (String raw : lines) {
            String line = raw.trim();
            if (!insideType) {
                if (TYPE_DECL_PATTERN.matcher(line).find()) insideType = true;
                continue;
            }
            if (line.startsWith("/**")) {
                inJavadoc = true;
                docBuf.setLength(0);
                String rest = line.substring(3);
                if (rest.endsWith("*/")) {
                    pendingDoc = cleanJavadoc(rest.substring(0, rest.length() - 2));
                    inJavadoc = false;
                } else {
                    docBuf.append(rest);
                }
                continue;
            }
            if (inJavadoc) {
                if (line.endsWith("*/")) {
                    docBuf.append(" ").append(line, 0, line.length() - 2);
                    pendingDoc = cleanJavadoc(docBuf.toString());
                    inJavadoc = false;
                } else {
                    docBuf.append(" ").append(line.replaceFirst("^\\*\\s?", ""));
                }
                continue;
            }
            if (line.startsWith("@")) continue;

            Matcher mm = PUBLIC_METHOD_PATTERN.matcher(line);
            if (mm.find()) {
                methods.add(new MethodInfo(mm.group(1).trim(), mm.group(2), mm.group(3).trim(), pendingDoc));
                pendingDoc = "";
            } else if (!line.isEmpty()) {
                pendingDoc = "";
            }
        }
        return methods;
    }

    private static List<String> extractEnumValues(String content) {
        Pattern p = Pattern.compile("enum\\s+\\w+\\s*\\{([\\s\\S]*?)\\}");
        Matcher m = p.matcher(content);
        if (!m.find()) return List.of();
        String body = m.group(1).split(";")[0].replaceAll("//.*", "");
        return Arrays.stream(body.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.split("[(\\s]")[0])
                .toList();
    }

    private static String firstQuoted(String s) {
        if (s == null) return "";
        Matcher m = QUOTED_STRING.matcher(s);
        return m.find() ? m.group(1) : "";
    }

    private static String normalizePath(String base, String sub) {
        String combined = (base + "/" + sub).replaceAll("/+", "/");
        if (!combined.startsWith("/")) combined = "/" + combined;
        return combined.length() > 1 && combined.endsWith("/") ? combined.substring(0, combined.length() - 1) : combined;
    }

    private static String cleanJavadoc(String s) {
        return s.replaceAll("\\*", " ").replaceAll("\\s+", " ").trim();
    }

    private static String summarizeAnnotations(List<String> anns) {
        if (anns.isEmpty()) return "-";
        return anns.stream().map(a -> "`" + a + "`").collect(Collectors.joining(", "));
    }

    private static String escapePipe(String s) {
        return s == null ? "" : s.replace("|", "\\|");
    }

    private record JavaFileInfo(Path file, String relativePath, String pkg, String kind, String name,
                                String classJavadoc, List<String> classAnnotations, String content, List<String> lines) {
        boolean hasAnnotation(String ann) {
            return classAnnotations.stream().anyMatch(a -> a.equals(ann));
        }
    }

    private record FieldInfo(String name, String type, String comment, String annotations) {}
    private record EndpointInfo(String httpMethod, String path, String handler, String summary) {}
    private record MethodInfo(String returnType, String name, String params, String javadoc) {}
}
