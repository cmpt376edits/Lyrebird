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

package moe.lyrebird.model.io;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class AsyncIO {

    private final Executor asyncIoExecutor;
    private final CachedIO cachedIO;

    @Autowired
    public AsyncIO(
            @Qualifier("asyncIoExecutor") final Executor asyncIoExecutor,
            final CachedIO cachedIO
    ) {
        this.asyncIoExecutor = asyncIoExecutor;
        this.cachedIO = cachedIO;
    }

    public CompletableFuture<Image> loadImage(final String imageUrl) {
        return CompletableFuture.supplyAsync(() -> cachedIO.loadImage(imageUrl), asyncIoExecutor);
    }

    public CompletableFuture<Image> loadImageMiniature(final String imageUrl, final double width, final double heigth) {
        return CompletableFuture.supplyAsync(
                () -> cachedIO.loadImageMiniature(imageUrl, width, heigth),
                asyncIoExecutor
        );
    }

    public CompletableFuture<Media> loadMedia(final String mediaUrl) {
        return CompletableFuture.supplyAsync(
                () -> cachedIO.loadMediaFile(mediaUrl),
                asyncIoExecutor
        );
    }

}
