package com.study.conexao_solidaria.service;

import com.study.conexao_solidaria.model.dto.UsuarioDtoLogin;
import com.study.conexao_solidaria.model.dto.UsuarioDtoResponse;
import com.study.conexao_solidaria.model.dto.UsuarioDtoSolicitacao;
import com.study.conexao_solidaria.model.dto.VoluntarioDtoId;
import com.study.conexao_solidaria.enums.Categorias;
import com.study.conexao_solidaria.exceptions.ServiceExc;
import com.study.conexao_solidaria.model.UsuarioModel;
import com.study.conexao_solidaria.repository.UsuarioRepository;
import com.study.conexao_solidaria.security.Criptografia;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;


    public List<UsuarioDtoResponse> buscarVoluntarios() {
        List<UsuarioModel> voluntarios = usuarioRepository.findByCategoria(Categorias.VOLUNTARIO);
        List<UsuarioDtoResponse> dtoVoluntarios = new ArrayList<>();
        UsuarioDtoResponse volunt;

        for (UsuarioModel v : voluntarios) {
            volunt = new UsuarioDtoResponse(v.getId()
                    , v.getCategoria(), v.getDeficiencias(), v.getNome(), v.getTelefone(),
                    v.getEmail(), v.getLatitude(),
                    v.getLongitude());
            dtoVoluntarios.add(volunt);
        }
        return dtoVoluntarios;
    }

    public List<VoluntarioDtoId> buscarVoluntarioId(Long id) {
        List<UsuarioModel> buscarUsuario = usuarioRepository.findAll();
        return buscarUsuario.stream().map(usuario -> new VoluntarioDtoId(usuario.getId(), usuario.getCategoria(), usuario.getDeficiencias(), usuario.getNome(), usuario.getTelefone(), usuario.getEmail(), usuario.getLatitude(), usuario.getLongitude(), usuario.getIdVoluntario())).collect(Collectors.toList());
    }

    public List<UsuarioDtoResponse> buscar() {
        List<UsuarioModel> buscarUsuario = usuarioRepository.findAll();
        return buscarUsuario.stream().map(usuario -> new UsuarioDtoResponse(usuario.getId(),
                usuario.getCategoria(), usuario.getDeficiencias(), usuario.getNome(), usuario.getTelefone(), usuario.getEmail(), usuario.getLatitude(),
                usuario.getLongitude())).collect(Collectors.toList());
    }

    public UsuarioDtoResponse buscarID(Long id) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("id não encontrado" + id));
        return new UsuarioDtoResponse(usuario.getId(),
                usuario.getCategoria(), usuario.getDeficiencias(), usuario.getNome(), usuario.getTelefone(), usuario.getEmail(), usuario.getLatitude(),
                usuario.getLongitude());
    }

    public boolean validadorDeMenorDeIdade(UsuarioModel usuarioModel) {
        LocalDate data = usuarioModel.getDataDeNascimento().plusYears(18);
        LocalDate now = LocalDate.now();
        return data.isBefore(now);
    }

    public UsuarioDtoResponse cadastrar(UsuarioModel usuarioModel) throws Exception {

        boolean validandoIdade = validadorDeMenorDeIdade(usuarioModel);
        usuarioModel.setSenha(Criptografia.md5(usuarioModel.getSenha()));
        if (validandoIdade) {
            usuarioRepository.save(usuarioModel);

            return new UsuarioDtoResponse(usuarioModel.getId()
                    , usuarioModel.getCategoria(), usuarioModel.getDeficiencias(), usuarioModel.getNome(),
                    usuarioModel.getTelefone(), usuarioModel.getEmail(), usuarioModel.getLatitude(),
                    usuarioModel.getLongitude());
        } else {
            return null;
        }
    }

    public UsuarioDtoResponse atualizar(UsuarioModel usuarioModel, Long id) {

        UsuarioModel atualizar = usuarioRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("ID not found " + id));
        atualizar.setLatitude(usuarioModel.getLatitude());
        atualizar.setLongitude(usuarioModel.getLongitude());

        usuarioRepository.save(atualizar);

        return new UsuarioDtoResponse(atualizar.getId()
                , atualizar.getCategoria(), atualizar.getDeficiencias(), atualizar.getNome(), atualizar.getTelefone(),
                atualizar.getEmail(), atualizar.getLatitude(),
                atualizar.getLongitude());
    }

    public void deletar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public UsuarioDtoSolicitacao solicitarAjuda(Long id) {

        double menorDistancia = Double.MAX_VALUE;
        UsuarioModel voluntarioMaisProximo = null;

        UsuarioModel usuarioSolicitante = usuarioRepository.findById(id).get();
        List<UsuarioModel> usuarioVoluntarios = usuarioRepository.findByCategoria(Categorias.VOLUNTARIO);

        for (UsuarioModel voluntario : usuarioVoluntarios) {
            double distancia = CalculadoresDeDistancia.calculaDistancia(usuarioSolicitante.getLatitude(),
                    usuarioSolicitante.getLongitude(),
                    voluntario.getLatitude(), voluntario.getLongitude());
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                voluntarioMaisProximo = voluntario;
            }
        }

        if (voluntarioMaisProximo == null) {
            throw new RuntimeException("Nenhum voluntário encontrado!");
        } else if (menorDistancia > 4000) {
            throw new NoSuchElementException("Nenhum voluntário encontrado!");
        }

        return new UsuarioDtoSolicitacao(voluntarioMaisProximo.getId(),
                voluntarioMaisProximo.getNome(),
                voluntarioMaisProximo.getTelefone());
    }

    public UsuarioDtoLogin loginUser(UsuarioModel usuarioModel) throws ServiceExc, NoSuchAlgorithmException {

        UsuarioModel usuario = usuarioRepository.buscarLogin(usuarioModel.getLogin(),
                Criptografia.md5(usuarioModel.getSenha()));

        if(usuario == null){
            return null;
        }
        return new UsuarioDtoLogin(usuario.getId(), usuario.getCategoria(), usuario.getNome());
    }

}