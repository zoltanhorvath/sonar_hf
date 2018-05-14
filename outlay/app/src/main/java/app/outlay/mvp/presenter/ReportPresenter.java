package app.outlay.mvp.presenter;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import app.outlay.core.executor.DefaultSubscriber;
import app.outlay.core.utils.DateUtils;
import app.outlay.domain.interactor.GetExpensesUseCase;
import app.outlay.domain.model.Report;
import app.outlay.mvp.view.StatisticView;

/**
 * Created by Bogdan Melnychuk on 1/21/16.
 */
public class ReportPresenter extends MvpBasePresenter<StatisticView> {
    private GetExpensesUseCase loadReportUseCase;

    @Inject
    ReportPresenter(
            GetExpensesUseCase loadReportUseCase
    ) {
        this.loadReportUseCase = loadReportUseCase;
    }

    public void getExpenses(Date date, int period) {

        Map<String, Date> dates = DateUtils.getDates(date, period);
        Date startDate = dates.get("start");
        Date endDate = dates.get("end");

        loadReportUseCase.execute(new GetExpensesUseCase.Input(startDate, endDate, null), new DefaultSubscriber<Report>() {
            @Override
            public void onNext(Report report) {
                super.onNext(report);
                getView().showReport(report);
            }
        });
    }
}