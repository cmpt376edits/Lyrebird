/*
 *     Lyrebird, a free open-source cross-platform twitter client.
 *     Copyright (C) 2017-2018, Tristan Deloche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package moe.lyrebird.view.components.directmessages;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import moe.tristan.easyfxml.api.FxmlController;
import moe.lyrebird.model.twitter.observables.DirectMessages;
import moe.lyrebird.view.components.cells.DirectMessageListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.User;

import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.Collections;
import java.util.function.Supplier;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(scopeName = SCOPE_PROTOTYPE)
public class DMConversationController implements FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(DMConversationController.class);

    @FXML
    private ListView<DirectMessage> messageListView;

    private final DirectMessages directMessages;
    private final Supplier<DirectMessageListCell> directMessageListCellSupplier;

    private final Property<User> palProperty;
    private final ListProperty<DirectMessage> loadedMessages;

    public DMConversationController(
            final DirectMessages directMessages,
            final ApplicationContext applicationContext
    ) {
        this.directMessages = directMessages;
        this.directMessageListCellSupplier = () -> applicationContext.getBean(DirectMessageListCell.class);
        this.palProperty = new SimpleObjectProperty<>(null);
        this.loadedMessages = new SimpleListProperty<>(FXCollections.observableList(Collections.emptyList()));
    }

    @Override
    public void initialize() {
        LOG.debug("Schedule displaying of conversation once senderId has been received!");
        messageListView.setCellFactory(messages -> directMessageListCellSupplier.get());
        palProperty.addListener((palVal, oldVal, newVal) -> {
            LOG.debug("Detected palProperty being set! New value : {}", newVal.getScreenName());
            loadConversationContents(newVal);
            messageListView.itemsProperty().bind(loadedMessages);
        });
    }

    public void setPal(User pal) {
        LOG.debug("Created conversation view for conversing with user!");
        LOG.debug("Mapping DM conversation view {} with senderId {}", this, pal.getScreenName());
        this.palProperty.setValue(pal);
    }

    private void loadConversationContents(final User user) {
        this.loadedMessages.setValue(FXCollections.observableList(directMessages.loadedConversations().get(user)));
    }

}
