package com.example.miniprojekt.controllers;

import com.example.miniprojekt.dto.WishlistItemDTO;
import com.example.miniprojekt.model.User;
import com.example.miniprojekt.model.Wishlist;
import com.example.miniprojekt.model.WishlistItem;
import com.example.miniprojekt.repository.WishlistRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@RequestMapping("/")
@Controller
public class WishlistController {
    WishlistRepository wishlistRepository;

    public WishlistController () { wishlistRepository = new WishlistRepository(); }

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email")String email, @RequestParam("pw") String pw, HttpSession session, Model model)
    {
        User user = wishlistRepository.getUser(email);
        if (user != null)
            if (user.getPassword().equals(pw)) {
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(60);
                return "redirect:/wishlists";
            }
        model.addAttribute("wrongLogin", true);
        return "index";
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<Wishlist>> getWishlists(){
        List<Wishlist> wishlists = wishlistRepository.getWishlists();
        return new ResponseEntity<>(wishlists, HttpStatus.OK);
    }

    @GetMapping(path = "/wishlists")
    public String getWishlists(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        List<Wishlist> wishlists = wishlistRepository.getUserWishlists(user.getId());
        model.addAttribute("wishlist", wishlists);
        return "wishlists";
    }

    @GetMapping("/signup")
    public String signUp(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        model.addAttribute("signUpError", "");
        return "index";
    }

    @PostMapping("/signup")
    public String addSignUp(@ModelAttribute("user") User user, HttpSession session, Model model) {
        if(user.getName().length() < 1 || user.getEmail().length() < 1 || user.getPassword().length() < 1) {
            model.addAttribute("signUpError", "Fill out all fields!");
            return "index";
        }
        try {
            session.setAttribute("user", user);
            wishlistRepository.addSignUp(user);
            return "redirect:/wishlists";
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                model.addAttribute("signUpError", "Email is already registered.");
            }
            return "index";
        }
    }


    @GetMapping("/create/wishlist")
    public String createWishlist(Model model) {
        Wishlist wishlist = new Wishlist();
        model.addAttribute("wishlist", wishlist);
        return "createWishlist";
    }

    @PostMapping("/create/wishlist")
    public String createWishlist(@ModelAttribute("wishlist") Wishlist wishlist) {
        wishlistRepository.createWishlist(wishlist);
        return "wishlistCreated";
    }

    @GetMapping("/addItem/{id}")
    public String addItem(@PathVariable int id, Model model) {
        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setWishlistId(id);
        model.addAttribute("wishlistItem", wishlistItem);
        return "addWishlistItem";
    }

    @PostMapping("/addItem")
    public String addItem(@ModelAttribute("wishlistItem") WishlistItem wishlistItem) {
        wishlistRepository.addWishlistItem(wishlistItem);
        return "redirect:/items/" + wishlistItem.getWishlistId();
    }

    @GetMapping(path = "/item2/{id}")
    public ResponseEntity<List<WishlistItem>> getItem(@PathVariable int id){
        List<WishlistItem> wishlistItems = wishlistRepository.getItem(id);
        return new ResponseEntity<>(wishlistItems, HttpStatus.OK);
    }

    @GetMapping(path = "/items/{id}")
    public String getItem(Model model, @PathVariable int id) {
        WishlistItemDTO wishlistItems = wishlistRepository.getWishlistItems(id);
        model.addAttribute("wishlistTitle", wishlistItems.getWishlistTitle());
        model.addAttribute("wishlistId", id);

        model.addAttribute("itemList", wishlistItems.getWishlistItems());
        return "wishlistItems";
    }

    @GetMapping(path = "/wishlist/{id}")
    public ResponseEntity<List<Wishlist>> getWishlist(@PathVariable int id){
        List<Wishlist> wishlist = wishlistRepository.getWishlist(id);
        return new ResponseEntity<>(wishlist, HttpStatus.OK);
    }

    @GetMapping("/delete/{id}")
    public String deleteSuperhero(@PathVariable int id){
        wishlistRepository.deleteWishlist(id);
        return "redirect:/wishlists";
    }
}
