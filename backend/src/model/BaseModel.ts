import {CreateIndexesOptions, IndexSpecification} from "mongodb";

export abstract class BaseModelInterface {
    static collectionName: string

    _id?: string
    createdAt: Date | undefined | string
    updatedAt: Date | undefined | string

    static indexed: ()=> Array<{idxSpec: IndexSpecification, idxOpt?: CreateIndexesOptions}>
}
