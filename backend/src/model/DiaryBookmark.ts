import {BaseModelInterface} from "./BaseModel";

export interface TDiaryBookmark {
    _id?: string
    diaryId: string;
    bookmarkId: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;
}

class DiaryBookmark extends BaseModelInterface implements TDiaryBookmark {
    static collectionName = "diaryBookmark"
    _id?: string
    diaryId: string;
    bookmarkId: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;

    constructor(d: TDiaryBookmark) {
        super()
        this.diaryId = d.diaryId
        this.bookmarkId = d.bookmarkId
        this.createdAt = d.createdAt
        this.updatedAt = d.updatedAt
    }
    static indexed(){
        return [
            {idxSpec: {diaryId: 1}},
            {idxSpec: {bookmarkId: 1}},
        ]
    }
}

export default DiaryBookmark
