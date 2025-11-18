package com.ucc.avisos;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Validated
public class AvisosController {

  private final ListadoRepository listadoRepo;
  private final NotaRepository notaRepo;
  private final SitioRepository sitioRepo;

  public AvisosController(ListadoRepository listadoRepo, NotaRepository notaRepo, SitioRepository sitioRepo){
    this.listadoRepo = listadoRepo;
    this.notaRepo = notaRepo;
    this.sitioRepo = sitioRepo;
  }

  // Salud para probar que el server está vivo sin tocar la BD
  @GetMapping("/health")
  @ResponseBody
  public Map<String,String> health(){
    Map<String,String> m = new HashMap<>();
    m.put("ok","true");
    return m;
  }

  // Listado con paginación
  @GetMapping({"/","/avisos"})
  public String avisos(@RequestParam(name="page", defaultValue="1") int page, Model model){
    int per = 5, offset = Math.max(0,(page-1)*per);
    var rows = sitioRepo.listado(per, offset);
    long total = sitioRepo.totalAvisos();
    var pages = Math.max(1,(int)Math.ceil(total/(double)per));
    model.addAttribute("avisos", rows);
    model.addAttribute("pagination", Map.of(
        "page", page, "pages", pages,
        "hasPrev", page>1, "prev", Math.max(1,page-1),
        "hasNext", page<pages, "next", Math.min(pages,page+1)
    ));
    model.addAttribute("pageTitle", "Listado");
    model.addAttribute("active", "avisos");   // pestaña activa
    return "listado";
  }

  // ---------- API Evaluación ----------
  // Promedio y cantidad para un aviso
  @GetMapping(value = "/api/notas")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getNotas(
          @RequestParam(name = "avisoId", required = false) Integer avisoId) {
      if (avisoId == null) {
          // Responder vacío (200) para evitar 400 en llamadas accidentales
          return ResponseEntity.ok(Map.of());
      }
      Double avg = listadoRepo.avgNota(avisoId);
      Long cnt   = listadoRepo.cntNota(avisoId);
      return ResponseEntity.ok(Map.of(
              "promedio", avg == null ? null : avg,
              "cantidad", cnt == null ? 0L : cnt
      ));
  }
  // Body esperado: { "avisoId": N, "nota": M } con 1 <= M <= 7
  public static record NotaReq(@NotNull Integer avisoId,
                               @NotNull @Min(1) @Max(7) Integer nota) {}

  @PostMapping(path="/api/notas", consumes="application/json", produces="application/json")
  @ResponseBody
  public ResponseEntity<?> postNota(@Valid @RequestBody NotaReq req){
    // Guardar voto
    Nota n = new Nota();
    n.setAvisoId(req.avisoId());
    n.setNota(req.nota());
    notaRepo.save(n);

    // Responder con el nuevo resumen (promedio y cantidad)
    Map<String,Object> out = new HashMap<>();
    out.put("ok", true);
    out.put("promedio", listadoRepo.avgNota(req.avisoId()));
    out.put("cantidad", listadoRepo.cntNota(req.avisoId()));
    return ResponseEntity.ok(out);
  }
  // ---------- /API Evaluación ----------

@GetMapping("/api/comentarios")
@ResponseBody
public ResponseEntity<?> apiComentarios(
        @RequestParam(name = "aviso_id", required = false) Integer avisoId){
    if (avisoId == null) return ResponseEntity.ok(List.of());
    return ResponseEntity.ok(sitioRepo.comentarios(avisoId));
}

  @GetMapping("/home")
  public String home(Model model){
    var items = sitioRepo.ultimos5();
    model.addAttribute("avisos", items);
    model.addAttribute("pageTitle", "Portada");
    model.addAttribute("active", "home");
    return "home";
  }

  @GetMapping("/detalle/{id}")
  public String detalle(@PathVariable("id") Integer id, Model model){
    var a = sitioRepo.detalle(id);
    var fotos = sitioRepo.fotos(id);
    model.addAttribute("a", a);
    model.addAttribute("fotos", fotos);
    model.addAttribute("pageTitle", "Detalle");
    model.addAttribute("active", "avisos");
    return "detalle";
  }

  @GetMapping("/agregar")
  public String agregar(Model model){
    model.addAttribute("pageTitle","Agregar");
    model.addAttribute("active","agregar");
    return "agregar";
  }

  @GetMapping("/estadisticas")
  public String estadisticas(Model model){
    model.addAttribute("pageTitle","Estadísticas");
    model.addAttribute("active","estad");
    return "estadisticas";
  }
}
