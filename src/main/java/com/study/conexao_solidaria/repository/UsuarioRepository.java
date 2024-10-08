package com.study.conexao_solidaria.repository;

import com.study.conexao_solidaria.enums.Categorias;
import com.study.conexao_solidaria.model.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    public List<UsuarioModel> findByCategoria(Categorias categorias);

    @Query("select m from UsuarioModel m where m.login = :login and m.senha = :senha")
    public UsuarioModel buscarLogin(String login, String senha);



}
