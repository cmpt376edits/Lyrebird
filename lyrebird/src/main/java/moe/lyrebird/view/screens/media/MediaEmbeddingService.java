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

package moe.lyrebird.view.screens.media;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import moe.lyrebird.view.screens.media.handlers.direct.DirectPhotoHandler;
import moe.lyrebird.view.screens.media.handlers.twitter.TwitterVideoHandler;
import twitter4j.MediaEntity;
import twitter4j.Status;

import javafx.scene.Node;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MediaEmbeddingService {

    public static final double EMBEDDED_MEDIA_RECTANGLE_SIDE = 64.0;
    public static final double EMBEDDED_MEDIA_RECTANGLE_CORNER_RADIUS = 10.0;

    private final DirectPhotoHandler directPhotoHandler;
    private final TwitterVideoHandler twitterVideoHandler;

    public MediaEmbeddingService(
            DirectPhotoHandler directPhotoHandler,
            TwitterVideoHandler twitterVideoHandler
    ) {
        this.directPhotoHandler = directPhotoHandler;
        this.twitterVideoHandler = twitterVideoHandler;
    }

    @Cacheable("embeddedNodes")
    public List<Node> embed(Status status) {
        return Arrays.stream(status.getMediaEntities())
                .filter(MediaEntityType::isSupported)
                .map(this::embedOne)
                .collect(Collectors.toList());
    }

    private Node embedOne(final MediaEntity entity) {
        switch (MediaEntityType.fromTwitterType(entity.getType())) {
            case PHOTO:
                return directPhotoHandler.handleMedia(entity.getMediaURLHttps());
            case VIDEO:
                return twitterVideoHandler.handleMedia(entity.getVideoVariants());
            case UNMANAGED:
            default:
                throw new IllegalArgumentException("Twitter type "+entity.getType()+" is not supported!");
        }
    }
}
