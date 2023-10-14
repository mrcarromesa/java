package br.com.mrcarromesa.todolist.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.mrcarromesa.todolist.users.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // Toda class que quero que o spring gerencie preciso adicionar essa annotation
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

        String[] pathsToValid = {"/tasks/"};

        List<String> listPathValid = Arrays.asList(pathsToValid);

        var servletPath = request.getServletPath();
        
        if (!listPathValid.contains(servletPath)) {
          filterChain.doFilter(request, response);
          return;
        }

        var authorization = request.getHeader("Authorization");

        var authEncoded = authorization.substring("Basic".length()).trim();

        byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

        var authString = new String(authDecoded);
        String[] credentials = authString.split(":");

        String userName = credentials[0];
        String password = credentials[1];

        var user = this.userRepository.findByUserName(userName);

        if (user == null) {
          response.sendError(HttpStatus.UNAUTHORIZED.value(), "User unauthorized");
          return;
        }
        
        var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        
        if(!passwordVerify.verified) {
          response.sendError(HttpStatus.UNAUTHORIZED.value(), "User unauthorized");
          return;
        }

        request.setAttribute("idUser", user.getId());

        filterChain.doFilter(request, response);
  }
  
}
