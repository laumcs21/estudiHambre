
package com.estudiHambre.service;

import com.estudiHambre.dto.usuario.UsuarioRegistroDTO;
import com.estudiHambre.dto.usuario.auth.LoginRespuestaDTO;

public interface AuthService {

    LoginRespuestaDTO registrar(UsuarioRegistroDTO dto);

    LoginRespuestaDTO obtenerRespuestaLogin(String correo);
}

