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
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    
    @Autowired
    public BookMstService(BookMstRepository bookMstRepository){
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
            // AccountDtoからAccountへの変換
            BookMst bookMst = new BookMst();
        

            
            bookMst.setTitle(bookMstDto.getTitle());
            bookMst.setIsbn(bookMstDto.getIsbn());

        

            // データベースへの保存
            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            throw e;
        }
    }
    public boolean isValidTitle(String title, Model model){

        
        // 書籍名が空白だった時
        if (StringUtils.isEmpty(title)) {
    
            model.addAttribute("errTitle", "書籍名を入力してください");
            return true;
        }
        // 書籍名の桁数が255文字以上のとき
        if (title.length() > 255) {
    
            model.addAttribute("errTitle", "書籍名は255文字以内で入力してください");
            return true;
        }
        return false;
    }
    // ISBNが入力されているか
    public boolean isValidIsbn(String isbn, Model model){
    
        // ISBNが空白だった時
        if (StringUtils.isEmpty(isbn)) {
    
            model.addAttribute("errIsbn", "ISBNを入力してください");
            return true;
        }
        // ISBNが13桁ではない
        if (isbn.length() != 13) {
    
            model.addAttribute("errIsbn", "ISBNは13文字で入力してください");
            return true;
        }
        // ISBNが半角数字以外で入力されている
        if (!isbn.matches("\\d+")) {
    
            model.addAttribute("errIsbn", "ISBNは半角数字で入力してください");
            return true;
        }
        return false;
    }
    // 重複チェック用のメソッド
    public boolean selectByIsbn(String isbn, Model model){
        List<BookMst> books = this.bookMstRepository.selectByIsbn(isbn);
    
        if (books.size() != 0) {
            model.addAttribute("errIsbn", "このISBNは既に登録されています");
            return true;
        }
        return false;
    }
    
}



