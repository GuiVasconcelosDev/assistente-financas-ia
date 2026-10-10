package com.gui.financas.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    /** Retorna null quando não há gastos no período. */
    @Query("select sum(g.valor) from Gasto g where g.data between :inicio and :fim")
    BigDecimal somarPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    /** Retorna null quando não há gastos no período para a categoria. */
    @Query("select sum(g.valor) from Gasto g "
            + "where g.categoria = :categoria and g.data between :inicio and :fim")
    BigDecimal somarPorCategoriaEPeriodo(@Param("categoria") CategoriaGasto categoria,
                                         @Param("inicio") LocalDate inicio,
                                         @Param("fim") LocalDate fim);

    /** Cada linha: [CategoriaGasto, BigDecimal], da maior soma para a menor. */
    @Query("select g.categoria, sum(g.valor) from Gasto g "
            + "where g.data between :inicio and :fim "
            + "group by g.categoria order by sum(g.valor) desc")
    List<Object[]> somarAgrupadoPorCategoria(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    List<Gasto> findAllByOrderByDataDescIdDesc(Pageable pageable);
}
