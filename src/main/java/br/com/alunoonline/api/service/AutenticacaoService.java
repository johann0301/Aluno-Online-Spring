package br.com.alunoonline.api.service;

import br.com.alunoonline.api.model.Usuario;
import br.com.alunoonline.api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioRepository repository;

    public AutenticacaoService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = repository.findByLogin(login);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado.");
        }
        return usuario;
    }
}
