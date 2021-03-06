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

package moe.lyrebird.model.twitter.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import io.vavr.CheckedFunction1;
import moe.lyrebird.model.sessions.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UploadedMedia;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static io.vavr.API.unchecked;

@Component
public class NewTweetService {

    private static final Logger LOG = LoggerFactory.getLogger(NewTweetService.class);

    private final SessionManager sessionManager;
    private final Executor twitterExecutor;

    @Autowired
    public NewTweetService(
            final SessionManager sessionManager,
            @Qualifier("twitterExecutor") final Executor twitterExecutor
    ) {
        LOG.debug("Initializing {}...", getClass().getSimpleName());
        this.twitterExecutor = twitterExecutor;
        this.sessionManager = sessionManager;
    }

    public CompletionStage<Status> sendNewTweet(final String content, final List<File> medias) {
        return CompletableFuture.supplyAsync(
                () -> sessionManager.getCurrentTwitter()
                                    .mapTry(twitter -> tweet(twitter, content, medias))
                                    .get()
                , twitterExecutor
        );
    }

    private static Status tweet(final Twitter twitter, final String content, final List<File> media) throws TwitterException {
        final List<Long> uploadedMediaIds = uploadMedias(twitter, media);
        final StatusUpdate statusUpdate = buildStatus(content, uploadedMediaIds);
        return twitter.updateStatus(statusUpdate);
    }

    private static StatusUpdate buildStatus(final String content, final List<Long> mediaIds) {
        LOG.debug("Preparing new tweet with content [\"{}\"] and mediaIds {}", content, mediaIds);
        final StatusUpdate statusUpdate = new StatusUpdate(content);

        final Long[] mediaIdsArr = mediaIds.toArray(new Long[0]);
        final long[] mediaUnboxedArr = new long[mediaIdsArr.length];
        for (int i = 0; i < mediaIdsArr.length; i++) {
            mediaUnboxedArr[i] = mediaIdsArr[i];
        }
        statusUpdate.setMediaIds(mediaUnboxedArr);

        return statusUpdate;
    }

    private static List<Long> uploadMedias(Twitter twitter, final List<File> attachments) {
        return attachments.stream()
                          .map(unchecked((CheckedFunction1<File, UploadedMedia>) twitter::uploadMedia))
                          .map(UploadedMedia::getMediaId)
                          .collect(Collectors.toList());
    }

}
