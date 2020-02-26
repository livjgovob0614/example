/*
 * Copyright (C) 2009 Roman Masek
 *
 * This file is part of OpenSudoku.
 *
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.moire.opensudoku.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;

import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.FolderInfo;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.game.command.CommandStack;
import org.moire.opensudoku.gui.SudokuListFilter;

/**
 * Wrapper around opensudoku's database.
 * <p/>
 * You have to pass application context when creating instance:
 * <code>SudokuDatabase db = new SudokuDatabase(getApplicationContext());</code>
 * <p/>
 * You have to explicitly close connection when you're done with database (see {@link #close()}).
 * <p/>
 * This class supports database transactions using {@link #beginTransaction()}, \
 * {@link #setTransactionSuccessful()} and {@link #endTransaction()}.
 * See {@link SQLiteDatabase} for details on how to use them.
 *
 * @author romario
 */
public class SudokuDatabase {
    public static final String DATABASE_NAME = "opensudoku";
    public static final String SUDOKU_TABLE_NAME = "sudoku";
    public static final String FOLDER_TABLE_NAME = "folder";

    public static final int GAME_STATE_COMPLETED = 1;
    public static final int GAME_STATE_PLAYING = 2;


    public SudokuDatabase(Context context) {
        mOpenHelper = new DatabaseHelper(context);
    }

    /**
     * Returns the full folder info - this includes count of games in particular states.
     *
     * @param folderID Primary key of folder.
     * @return
     */
    public FolderInfo getFolderInfoFull(long folderID) {
        FolderInfo folder = null;

        SQLiteDatabase db;
        String q = "select folder._id as _id, folder.name as name, sudoku.state as state, count(sudoku.state) as count from folder left join sudoku on folder._id = sudoku.folder_id where folder._id = " + folderID + " group by sudoku.state";

	//////////
        db = getReadableDatabase();
        try (Cursor c = db.rawQuery(q, null)) {

            // selectionArgs: You may include ?s in where clause in the query, which will be replaced by the values from selectionArgs. The values will be bound as Strings.

            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
                String name = c.getString(c.getColumnIndex(FolderColumns.NAME));
                int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
                int count = c.getInt(c.getColumnIndex("count"));

                if (folder == null) {
                    folder = new FolderInfo(id, name);
                }

                folder.puzzleCount += count;
                if (state == GAME_STATE_COMPLETED) {
                    folder.solvedCount += count;
                }
                if (state == GAME_STATE_PLAYING) {
                    folder.playingCount += count;
                }
            }
        }

        return folder;
    }
