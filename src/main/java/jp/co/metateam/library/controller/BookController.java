package jp.co.metateam.library.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
        model.addAttribute("bookMstList", bookMstList);
        return "book/index";
 
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }
    
        return "book/add";
    }
  
    
    @PostMapping("/book/add")
    public String register(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra,Model model) {
        try {
            boolean ValidTitle = bookMstService.isValidTitle(bookMstDto.getTitle(), model);
            if (ValidTitle) {
                model.addAttribute("bookMstDto", bookMstDto);
            }
            boolean ValidIsbn = bookMstService.isValidIsbn(bookMstDto.getIsbn(), model);
            if (ValidIsbn) {
                model.addAttribute("bookMstDto", bookMstDto);
            }
            if (ValidTitle||ValidIsbn) {
                return "/book/add";
            }
            boolean isbnExist = bookMstService.selectByIsbn(bookMstDto.getIsbn(), model);
            if (isbnExist) {
                model.addAttribute("bookMstDto", bookMstDto);
                return "/book/add";
            }


            
                
            this.bookMstService.save(bookMstDto);
            
            return "redirect:/book/index";
        } catch (Exception e) {
            return "redirect:/book/add";
        }
        

     }

    }

        

    

    


