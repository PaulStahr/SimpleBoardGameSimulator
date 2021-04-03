package gameObjects.definition;

import data.Texture;
import gameObjects.columnTypes.GameObjectBooksColumnType;
import gameObjects.columnTypes.GameObjectColumnType;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

import java.util.List;

public class GameObjectBook extends GameObject{

    public static final List<TableColumnType> BOOK_ATTRIBUTES = ArrayTools.unmodifiableList(new TableColumnType[]{GameObjectBooksColumnType.ID, GameObjectBooksColumnType.NAME, GameObjectBooksColumnType.DELETE});

    public static class BookSide{
        public final int value;
        public Texture img;
        public final String imgStr;


        public BookSide(int value, Texture img, String imgStr){
            this.value = value;
            this.img = img;
            this.imgStr = imgStr;
        }

        public BookSide(BookSide other) {
            this.value = other.value;
            this.img = new Texture(other.img);
            this.imgStr = other.imgStr;
        }
    }
    public BookSide bss[];

    public GameObjectBook(String uniqueObjectName, String objectType, int widthInMM, int heightInMM, GameObjectBook.BookSide sides[], int value, int sortValue, int rotationStep){
        super(uniqueObjectName, objectType, widthInMM, heightInMM, value, sortValue, rotationStep, 0);
        this.bss = sides;
    }

    public GameObjectBook(GameObjectBook other) {
        super(other);
        this.bss = new BookSide[other.bss.length];
        for (int i = 0; i < bss.length; ++i)
        {
            this.bss[i] = new BookSide(other.bss[i]);
        }
    }

    @Override
    public Texture getLook(ObjectState state, int playerId) {
        return bss[((GameObjectBook.BookState)state).side].img;
    }

    @Override
    public ObjectState newObjectState() {
        return new BookState();
    }

    @Override
    public void updateImages(GameInstance gi) {
        for (int i = 0; i < bss.length; ++i)
        {
            bss[i].img = gi.game.getImage(bss[i].imgStr);
        }
    }

    public static class BookState extends ObjectState
    {
        /**
         *
         */
        private static final long serialVersionUID = 2534848632779449228L;
        public int side = 0;
        public boolean unfold = false;

        public BookState() {}

        public BookState(GameObjectBook.BookState bookState) {
            set(bookState);
        }


        @Override
        public int hashCode()
        {
            return super.hashCode() ^ side;
        }


        @Override
        public void set(ObjectState state)
        {
            super.set(state);
            side = ((GameObjectBook.BookState)state).side;
            unfold = ((GameObjectBook.BookState)state).unfold;
        }


        @Override
        public ObjectState copy() {
            return new GameObjectBook.BookState(this);
        }

        @Override
        public void reset() {
            super.reset();
            side = 0;
            unfold = false;
        }
    }

    public BookSide nextPage(BookState state){
        state.side = (state.side+1) % bss.length;
        state.value = bss[state.side].value;
        return bss[state.side];
    }

    public BookSide previousPage(BookState state){
        state.side = (state.side-1 + bss.length) % bss.length;
        state.value = bss[state.side].value;
        return bss[state.side];
    }

    @Override
    public GameObject copy() {return new GameObjectBook(this);}
}
