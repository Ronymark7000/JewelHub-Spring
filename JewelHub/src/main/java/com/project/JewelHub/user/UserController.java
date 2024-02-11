package com.project.JewelHub.user;

import com.project.JewelHub.config.JwtService;
import com.project.JewelHub.user.dtos.UserDto;
import com.project.JewelHub.user.dtos.UserLoginDto;
import com.project.JewelHub.user.dtos.UserRegisterDto;
import com.project.JewelHub.user.dtos.UserResDto;
import com.project.JewelHub.util.ResponseWrapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    @Value("${cookie.expire.date}")
    private int COOKIE_EXPIRE;

    private final UserService userService;

    private final JwtService jwtService;

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseWrapper<UserResDto>> register(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        userRegisterDto.setRole("User");
        UserResDto user = userService.registerUser(userRegisterDto);
        return ResponseEntity.status(200).body(new ResponseWrapper<>(true, 200, "Register Successful", user));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResponseWrapper<UserResDto>> login(@Valid @RequestBody UserLoginDto userLoginDto, HttpServletResponse response) {
        UserResDto user = userService.getUserByEmailAndPassword(userLoginDto.getEmail(), userLoginDto.getPassword());

        String token = jwtService.generateToken(user);
        final Cookie cookie = new Cookie("token", token);
        cookie.setSecure(false);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(COOKIE_EXPIRE);
        cookie.setPath("/api");
        response.addCookie(cookie);
        System.out.println(token);
        return ResponseEntity.status(200).body(new ResponseWrapper<>(true, 200, "Login Success",null));

    }

    @GetMapping("/admin/users")
    public ResponseWrapper<User> getAllUserForAdmin() {
        List<UserDto> user = userService.getAllUsers();
        ResponseWrapper response = new ResponseWrapper();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Users retrieved successfully");
        response.setResponse(user);
        return response;
    }

    @GetMapping("/user")
    public ResponseEntity<ResponseWrapper<List<UserDto>>> getAllUser() {
        ResponseWrapper<List<UserDto>> response = new ResponseWrapper<>();
        try {
            List<UserDto> users = userService.getAllUsers();
            response.setStatusCode(HttpStatus.OK.value());
            response.setSuccess(true);
            response.setMessage("Users retrieved successfully");
            response.setResponse(users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.NOT_FOUND.value());
            response.setSuccess(false);
            response.setMessage("Unexpected error while showing list of users");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseWrapper<UserDto>> getUserById(@PathVariable("userId") int userId) {
        ResponseWrapper<UserDto> response = new ResponseWrapper<>();
        try {
            UserDto user = userService.getUserById(userId);
            if (user != null) {
                response.setStatusCode(HttpStatus.OK.value());
                response.setSuccess(true);
                response.setMessage("User retrieved successfully");
                response.setResponse(user);
                return ResponseEntity.ok(response);
            } else {
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                response.setMessage("User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception ex) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Unexpected error while finding user ID");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/user")
    public ResponseWrapper<User> createUser(@Valid @RequestBody User user){
        ResponseWrapper response = new ResponseWrapper();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage("Successfully Registered where User Id: " + user.getUserId() );
        response.setResponse(userService.createUser(user));
        return response;
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<ResponseWrapper<User>> updateUser(@PathVariable("userId") int userId, @Valid @RequestBody User user) {
        ResponseWrapper<User> response = new ResponseWrapper<>();
        try {
            User newUser = userService.updateUser(userId, user);
            if (newUser != null) {
                response.setStatusCode(HttpStatus.OK.value());
                response.setSuccess(true);
                response.setMessage("User updated successfully");
                response.setResponse(newUser);
                return ResponseEntity.ok(response);
            } else {
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                response.setMessage("User not Found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.setStatusCode(HttpStatus.NOT_FOUND.value());
            response.setMessage("Cannot update user due to unexpected error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /*@DeleteMapping("/user/{userId}")
    public ResponseWrapper<User> deleteUser(@PathVariable("userId")int userId){
        userService.deleteUser(userId);

        ResponseWrapper response = new ResponseWrapper();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("User deleted successfully");
        return response;
    }*/

}
