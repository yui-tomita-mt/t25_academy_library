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
import org.springframework.web.bind.annotation.RequestParam;
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
    public BookController(BookMstService bookMstService) {
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

    @GetMapping("/book/edit/{id}")
    public String editBook(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        BookMst book = bookMstService.selectById(id); // IDから書籍を取得
        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定された書籍は存在しません。");
            return "redirect:/book/index"; // 一覧画面htmlに遷移
        }
        model.addAttribute("book", book); // ビューに渡す
        return "book/edit"; // 編集用htmlへ

    }

    @PostMapping("/book/edit/{id}")
    public String updateBook(@PathVariable Long id, @RequestParam String title, @RequestParam String isbn,
            Model model, RedirectAttributes redirectAttributes) {
        // IDから書籍を取得
        BookMst book = bookMstService.selectById(id);
        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定された書籍は存在しません。");
            return "redirect:/book/index"; // 編集画面に遷移
        }

        // 変更がない場合
        if (book.getTitle().equals(title) && book.getIsbn().equals(isbn)) {
            model.addAttribute("errorMessage", "変更内容がありません。");
            model.addAttribute("book", book); // 編集画面に元のデータを再表示
            return "book/edit";
        }
        // 書籍名の変更チェック
        boolean isValidTitle = false;
        if (!book.getTitle().equals(title)) {
            // 変更があればバリデーションチェック
            isValidTitle = bookMstService.isValidTitle(title, model);
        }

        // ISBNの変更チェック
        boolean isValidIsbn = false;
        boolean isbnExist = false;
        if (!book.getIsbn().equals(isbn)) {
            // 変更があればバリデーション、無ければ次へ
            isValidIsbn = bookMstService.isValidIsbn(isbn, model);
            if (!isValidIsbn) {
                isbnExist = bookMstService.selectByIsbn(isbn, model);
            }
        }
        // あればエラーを編集画面に表示
        if (isValidTitle || isValidIsbn || isbnExist) {
            model.addAttribute("book", book);
            return "/book/edit";
        }

        // 書籍の更新処理
        BookMstDto bookDto = new BookMstDto();
        bookDto.setId(id);
        bookDto.setTitle(title);
        bookDto.setIsbn(isbn);
        bookMstService.updateBook(bookDto);

        return "redirect:/book/index"; // 一覧画面にリダイレクト
    }

    @PostMapping("/book/add")
    public String register(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra,
            Model model) {
        try {
            // 入力された書籍名にエラーがないか
            boolean isValidTitle = bookMstService.isValidTitle(bookMstDto.getTitle(), model);

            // 入力されたISBNにエラーがないか
            boolean isValidIsbn = bookMstService.isValidIsbn(bookMstDto.getIsbn(), model);

            boolean isbnExist = false;
            // 重複チェック
            if (!isValidIsbn) {
                // ISBNがすでにデータベースにあるか調べる
                isbnExist = bookMstService.selectByIsbn(bookMstDto.getIsbn(), model);

            }
            // 書籍名・ISBNどちらかにエラーがある場合は遷移しない
            if (isValidTitle || isValidIsbn || isbnExist) {
                model.addAttribute("bookMstDto", bookMstDto);
                return "/book/add";
            }

            // エラーなしの場合DBに登録
            this.bookMstService.save(bookMstDto);

            // 一覧画面に遷移
            return "redirect:/book/index";

        } catch (Exception e) {

            return "redirect:/book/add";
        }
    }

}
