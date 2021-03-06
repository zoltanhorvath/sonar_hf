package app.outlay.mvp.view;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.math.BigDecimal;
import java.util.List;

import app.outlay.domain.model.Category;
import app.outlay.domain.model.Expense;

/**
 * Created by bmelnychuk on 10/25/16.
 */

public interface EnterExpenseView extends MvpView {
    void showCategories(List<Category> categoryList);

    void showTimeline(List<Expense> expenses);

    void setAmount(BigDecimal amount);

    void alertExpenseSuccess(Expense expense);
}
