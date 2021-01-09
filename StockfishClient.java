/* Copyright 2018 David Cai Wang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import xyz.niflheim.stockfish.engine.Stockfish;
import xyz.niflheim.stockfish.engine.enums.Option;
import xyz.niflheim.stockfish.engine.enums.Query;
import xyz.niflheim.stockfish.engine.enums.Variant;
import xyz.niflheim.stockfish.exceptions.StockfishInitException;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockfishClient {
    private final Queue<Stockfish> engines;

    public StockfishClient(String path, int instances, Variant variant, Set<Option> options) throws StockfishInitException {
        ExecutorService executor = Executors.newFixedThreadPool(instances);
        ExecutorService callback = Executors.newSingleThreadExecutor();
        engines = new ArrayBlockingQueue<>(instances);

        for (int i = 0; i < instances; i++)
            engines.add(new Stockfish(path, variant, options.toArray(new Option[0])));
    }
    public String submit(Query query) {
        Stockfish engine = engines.peek();
        return switch (query.getType()) {
            case Best_Move -> engine.getBestMove(query);
            case Make_Move -> engine.makeMove(query);
            case Legal_Moves -> engine.getLegalMoves(query);
            case Checkers -> engine.getCheckers(query);
        };
    }

    public static class Builder {
        private final Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = null;
        private int instances = 1;

        public final Builder setInstances(int num) {
            instances = num;
            return this;
        }

        public final Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public final StockfishClient build() throws StockfishInitException {
            return new StockfishClient(path, instances, variant, options);
        }
    }
}
