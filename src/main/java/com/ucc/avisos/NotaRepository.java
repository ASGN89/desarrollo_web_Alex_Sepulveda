package com.ucc.avisos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotaRepository extends JpaRepository<Nota, Integer> {

    @Query("select coalesce(avg(n.nota), 0) from Nota n where n.avisoId = :avisoId")
    Double avgByAvisoId(Integer avisoId);

    @Query("select count(n) from Nota n where n.avisoId = :avisoId")
    Long countByAvisoId(Integer avisoId);
}
