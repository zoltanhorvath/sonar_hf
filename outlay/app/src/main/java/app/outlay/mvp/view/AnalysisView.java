package app.outlay.mvp.view;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import app.outlay.domain.model.Category;
import app.outlay.domain.model.Report;

/**
 * Created by bmelnychuk on 2/10/17.
 */

public interface AnalysisView extends MvpView {
    void showAnalysis(Report report);

    void setCategories(List<Category> categories);
}
