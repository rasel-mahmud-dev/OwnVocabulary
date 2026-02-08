import {slugify} from "../utils/slugify";
import {BaseModelInterface} from "./BaseModel";

export interface TDiary {
    _id?: string
    slug?: string;
    content: string;
    isPrivate: boolean;
    thumb: string;
    title: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;
}

class Diary extends BaseModelInterface implements TDiary {
    static collectionName = "diary"
    _id?: string
    slug?: string;
    content: string;
    isPrivate: boolean
    thumb: string;
    title: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;

    constructor(d: TDiary) {
        super()
        this.title = d.title
        this.slug = slugify(d.title) || d.title
        this.content = d.content
        this.isPrivate = d.isPrivate
        this.thumb = d.thumb
        this.createdAt = d.createdAt
        this.updatedAt = d.updatedAt
    }

    static indexed() {
        return [
            {idxSpec: {slug: 1}, idxOpt: {unique: true}},
            {idxSpec: {createdAt: -1}},
        ]
    }
}

export default Diary
