import {BaseModelInterface} from "./BaseModel";

export interface TDiaryTag {
    _id?: string
    diaryId: string;
    tag: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;
}

class DiaryTag extends BaseModelInterface implements TDiaryTag {
    static collectionName = "diaryTag"
    _id?: string
    diaryId: string;
    tag: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;

    constructor(d: TDiaryTag) {
        super()
        this.diaryId = d.diaryId
        this.tag = d.tag
        this.createdAt = d.createdAt
        this.updatedAt = d.updatedAt
    }

    static indexed(){
        return [
            {idxSpec: {diaryId: 1}},
            {idxSpec: {tag: 1}},
        ]
    }
}

export default DiaryTag
