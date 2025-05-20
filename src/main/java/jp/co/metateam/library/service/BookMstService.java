package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    public BookMst selectById(Long id) {
        return bookMstRepository.findById(id).orElse(null);
    }

    private final BookMstRepository bookMstRepository;

    @Autowired
    public BookMstService(BookMstRepository bookMstRepository) {
        this.bookMstRepository = bookMstRepository;
    }

    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            // DTO → Entityへの変換
            BookMst bookMst = new BookMst();
            bookMst.setTitle(bookMstDto.getTitle());
            bookMst.setIsbn(bookMstDto.getIsbn());

            // データベース保存
            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            // 例外は上位に投げてコントローラー側で処理
            throw e;
        }
    }

    @Transactional
    public void updateBook(BookMstDto bookMstDto) {
        try {
            // DTO → Entityへの変換
            BookMst bookMst = new BookMst();
            bookMst.setId(bookMstDto.getId());
            bookMst.setTitle(bookMstDto.getTitle());
            bookMst.setIsbn(bookMstDto.getIsbn());
            bookMst.setDeletedFlag(bookMstDto.getDeletedFlag());

            // データベース更新
            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            // 例外は上位に投げてコントローラー側で処理
            throw e;
        }
    }

    // タイトルバリデーション
    public boolean isValidTitle(String title, Model model) {
        if (StringUtils.isBlank(title)) {
            model.addAttribute("errTitle", "書籍名を入力してください");
            return true;
        }
        if (title.length() > 255) {
            model.addAttribute("errTitle", "書籍名は255文字以内で入力してください");
            return true;
        }
        return false;
    }

    // ISBNバリデーション
    public boolean isValidIsbn(String isbn, Model model) {
        List<String> isbnErrorlist = new ArrayList<String>();
        boolean isVlidError = false;
        // ISBNが空白だった時
        if (StringUtils.isBlank(isbn)) {
            isbnErrorlist.add("ISBNを入力してください");
            // model.addAttribute("errIsbn", "ISBNを入力してください");
            isVlidError = true;
        }

        // ISBNが13桁ではない
        if (isbn.length() != 13) {
            isbnErrorlist.add("ISBNは13文字で入力してください");
            // model.addAttribute("errIsbn", "ISBNは13文字で入力してください");
            isVlidError = true;
        }
        // ISBNが半角数字以外で入力されている
        if (!isbn.matches("\\d+")) {
            isbnErrorlist.add("ISBNは半角数字で入力してください");
            // model.addAttribute("errIsbn", "ISBNは半角数字で入力してください");
            isVlidError = true;
        }
        // エラーリストのサイズが1件以上あった場合
        // リストを画面に表示できるように変換
        if (!isbnErrorlist.isEmpty()) {
            model.addAttribute("isbnErrorlist", isbnErrorlist);
        }

        return isVlidError;
    }

    public boolean selectByIsbn(String isbn, Model model) {
        List<BookMst> selectIsbn = this.bookMstRepository.selectByIsbn(isbn);
        // selectIsbnに値が入っている場合は重複あり/エラーを表示しTrueを返却
        if (!selectIsbn.isEmpty()) {
            model.addAttribute("isbnErrorlist", "すでに登録されているISBNです");
            return true;
        }
        // selectIsbnに値が入っていない場合は重複なし/Falseを返却
        return false;
    }

    // 論理削除処理
    public void logicalDelete(Long id) {
        BookMst book = bookMstRepository.selectById(id).orElse(null);
        if (book != null && !book.isDeletedFlag()) {
            book.setDeletedFlag(true);//フラグを１にする
            bookMstRepository.save(book);
        }
    }

}