package moe.lyrebird.view.components.timeline;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import moe.tristan.easyfxml.api.FxmlController;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import moe.lyrebird.model.twitter.observables.Timeline;
import moe.lyrebird.view.components.cells.TweetListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.ScrollEvent;

import java.util.function.Supplier;

/**
 * Created by tristan on 03/03/2017.
 */
@Component
public class TimelineController implements FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(TimelineController.class);

    @FXML
    private ListView<Status> tweetsListView;

    private final Timeline timeline;
    private final Supplier<TweetListCell> tweetListCell;
    private final ListProperty<Status> tweetsProperty;

    public TimelineController(final Timeline timeline, final ApplicationContext context) {
        this.timeline = timeline;
        this.tweetsProperty = new ReadOnlyListWrapper<>(timeline.loadedTweets());
        this.tweetListCell = () -> context.getBean(TweetListCell.class);
    }

    @Override
    public void initialize() {
        bindUi();
        autoloadMoreTweets();
    }

    private void bindUi() {
        tweetsListView.setCellFactory(statuses -> tweetListCell.get());
        LOG.debug("Binding displayed tweets to displayable tweets...");
        tweetsListView.itemsProperty().bind(tweetsProperty);
        LOG.debug("Binded.");
    }

    @SuppressWarnings("unchecked")
    private void autoloadMoreTweets() {
        tweetsListView.addEventFilter(ScrollEvent.SCROLL, event -> {
            final ListViewSkin<Status> ts = (ListViewSkin<Status>) tweetsListView.getSkin();
            final VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
            final int lastVisible = vf.getLastVisibleCell().getIndex();
            final int lastPossible = tweetsListView.getItems().size() - 1;
            final boolean scrolledToEnd = lastVisible == lastPossible;
            if (scrolledToEnd) {
                LOG.debug("Scrolled to end [{}/{}]. Requesting more tweets.", lastVisible, lastPossible);
                timeline.loadMoreTweets();
            }
        });
    }

}
