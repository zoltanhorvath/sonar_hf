package app.outlay.mvp.presenter;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import java.util.List;

import javax.inject.Inject;

import app.outlay.core.executor.DefaultSubscriber;
import app.outlay.domain.interactor.GetCategoriesUseCase;
import app.outlay.domain.interactor.UpdateCategoriesUseCase;
import app.outlay.domain.model.Category;
import app.outlay.mvp.view.CategoriesView;

/**
 * Created by Bogdan Melnychuk on 1/21/16.
 */
public class CategoriesPresenter extends MvpBasePresenter<CategoriesView> {
    private GetCategoriesUseCase getCategoriesUseCase;
    private UpdateCategoriesUseCase updateCategoriesUseCase;

    @Inject
    CategoriesPresenter(
            GetCategoriesUseCase getCategoriesUseCase,
            UpdateCategoriesUseCase updateCategoriesUseCase
    ) {
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.updateCategoriesUseCase = updateCategoriesUseCase;
    }

    public void getCategories() {
        getCategoriesUseCase.execute(new DefaultSubscriber<List<Category>>() {
            @Override
            public void onNext(List<Category> categories) {
                getView().showCategories(categories);
            }
        });
    }

    public void updateOrder(List<Category> categories) {
        updateCategoriesUseCase.execute(categories, new DefaultSubscriber());
    }
}
