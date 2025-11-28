import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class Servidor {

    private static Connection con;

    public static void main(String[] args) throws Exception {

        // Conectar ao SQLite (arquivo conteudo.db na pasta do projeto)
        con = DriverManager.getConnection("jdbc:sqlite:conteudo.db");

        // Criar tabela (se não existir)
        String sql = "CREATE TABLE IF NOT EXISTS dados (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT," +
                "descricao TEXT," +
                "data TEXT," +
                "curtida TEXT" +
                ")";
        con.createStatement().execute(sql);

        // Criar servidor HTTP
        HttpServer s = HttpServer.create(new InetSocketAddress(8082), 0);

        // Rotas básicas
        s.createContext("/", t -> enviar(t, "login.html"));   // mostra login
        s.createContext("/login", Servidor::login);           // processa login
        s.createContext("/produtor", Servidor::produtor);     // cadastro
        s.createContext("/consumidor", Servidor::consumidor); // lista cards
        s.createContext("/avaliar", Servidor::avaliar);       // curtir / não curtir
        s.createContext("/deletar", Servidor::deletar);       // deletar
        s.createContext("/estilo.css", t -> enviarCSS(t, "estilo.css")); // CSS
        s.createContext("/aluno.png", t -> enviarImagem(t, "aluno.png")); // IMAGEM
        s.createContext("/professor.png", t -> enviarImagem(t, "professor.png")); // IMAGEM
        s.createContext("/atividade.png", t -> enviarImagem(t, "atividade.png")); // IMAGEM


        s.start();
        System.out.println("Servidor rodando em http://localhost:8082/");
    }

    // -------------------- LOGIN --------------------

    private static void login(HttpExchange t) throws IOException {
        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            enviar(t, "login.html");
            return;
        }

        String corpo = ler(t); // exemplo: tipo=produtor
        corpo = URLDecoder.decode(corpo, StandardCharsets.UTF_8);

        if (corpo.contains("produtor")) {
            redirecionar(t, "/produtor");
        } else {
            redirecionar(t, "/consumidor");
        }
    }

    // -------------------- PRODUTOR --------------------

    private static void produtor(HttpExchange t) throws IOException {

        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            enviar(t, "produtor.html");
            return;
        }

        String c = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);

        String nome = pega(c, "nome");
        String desc = pega(c, "descricao");
        String data = pega(c, "data");

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO dados (nome, descricao, data, curtida) VALUES (?,?,?,?)")) {

            ps.setString(1, nome);
            ps.setString(2, desc);
            ps.setString(3, data);
            ps.setString(4, "nenhuma"); // ainda não curtido
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        redirecionar(t, "/produtor");
    }

    // -------------------- CONSUMIDOR (lista todos os cards) --------------------

    private static void consumidor(HttpExchange t) throws IOException {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>Consumidor dos dados</title>");
        html.append("<link rel=\"stylesheet\" href=\"/estilo.css\">");
        html.append("</head><body>");

        html.append("<h1>Consumidor</h1>");
        html.append("<p>Cada atividade aparece em um card separado.</p>");

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nome, descricao, data, curtida FROM dados ORDER BY id DESC")) {

            boolean vazio = true;

            while (rs.next()) {
                vazio = false;

                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String desc = rs.getString("descricao");
                String data = rs.getString("data");
                String curtida = rs.getString("curtida");

                // Classe extra para cor do card
                String classeExtra = "";
                if ("curtir".equals(curtida)) {
                    classeExtra = " card-curtido";
                } else if ("nao".equals(curtida)) {
                    classeExtra = " card-nao-curtido";
                }

                html.append("<div class=\"card").append(classeExtra).append("\">");
                html.append("<img src=\"/atividade.png\" width=\"120\">");
                html.append("<p><strong>ID:</strong> ").append(id).append("</p>");
                html.append("<p><strong>Nome:</strong> ").append(nome).append("</p>");
                html.append("<p><strong>Descrição:</strong> ").append(desc).append("</p>");
                html.append("<p><strong>Data:</strong> ").append(data).append("</p>");
                html.append("<p><strong>Status:</strong> ").append(curtida).append("</p>");

                // Botão CURTIR
                html.append("<form method=\"POST\" action=\"/avaliar\">");
                html.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\">");
                html.append("<input type=\"hidden\" name=\"acao\" value=\"curtir\">");
                html.append("<button type=\"submit\">Curtir</button>");
                html.append("</form>");

                // Botão NÃO CURTIR
                html.append("<form method=\"POST\" action=\"/avaliar\">");
                html.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\">");
                html.append("<input type=\"hidden\" name=\"acao\" value=\"nao\">");
                html.append("<button type=\"submit\">Não curtir</button>");
                html.append("</form>");

                // Botão DELETAR
                html.append("<form method=\"POST\" action=\"/deletar\">");
                html.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\">");
                html.append("<input type=\"hidden\" name=\"acao\" value=\"nao\">");
                html.append("<button type=\"submit\">Deletar</button>");
                html.append("</form>");

                html.append("</div>");
            }

            if (vazio) {
                html.append("<p>Nenhuma atividade cadastrada ainda.</p>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            html.append("<p>Erro ao carregar atividades.</p>");
        }

        html.append("</body></html>");

        // Enviar HTML gerado
        byte[] b = html.toString().getBytes(StandardCharsets.UTF_8);
        t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    // -------------------- AVALIAR (curtir / não curtir um card específico) --------------------

    private static void avaliar(HttpExchange t) throws IOException {

        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            redirecionar(t, "/consumidor");
            return;
        }

        String corpo = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        String acao = pega(corpo, "acao"); // "curtir" ou "nao"
        String idStr = pega(corpo, "id");

        try {
            int id = Integer.parseInt(idStr);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE dados SET curtida = ? WHERE id = ?")) {
                ps.setString(1, acao);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        redirecionar(t, "/consumidor");
    }

    // -------------------- DELETAR --------------------

    private static void deletar(HttpExchange t) throws IOException {

        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            redirecionar(t, "/consumidor");
            return;
        }

        String corpo = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        String acao = pega(corpo, "acao"); // "curtir" ou "nao"
        String idStr = pega(corpo, "id");

        try {
            int id = Integer.parseInt(idStr);

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM dados WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        redirecionar(t, "/consumidor");
    }

    // -------------------- ENVIAR IMAGEM --------------------

    private static void enviarImagem(HttpExchange t, String arquivo) throws IOException {
        File f = new File("src/main/java/" + arquivo);

        byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "image/png");
        t.sendResponseHeaders(200, bytes.length);
        t.getResponseBody().write(bytes);
        t.close();
    }


    // -------------------- Funções auxiliares --------------------

    private static String pega(String corpo, String campo) {
        // corpo no formato: campo1=valor1&campo2=valor2...
        for (String s : corpo.split("&")) {
            String[] p = s.split("=");
            if (p.length == 2 && p[0].equals(campo)) return p[1];
        }
        return "";
    }

    private static String ler(HttpExchange t) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8)
        );
        String linha = br.readLine();
        return (linha == null) ? "" : linha;
    }

    private static void enviar(HttpExchange t, String arq) throws IOException {
        File f = new File("src/main/java/" + arq);
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void enviarCSS(HttpExchange t, String arq) throws IOException {
        File f = new File("src/main/java/" + arq);
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "text/css; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void redirecionar(HttpExchange t, String rota) throws IOException {
        t.getResponseHeaders().add("Location", rota);
        t.sendResponseHeaders(302, -1);
        t.close();
    }
}
