import RepositoryBase from "../respository/repository.base";
import { Request, Response } from "express"
import date from "../utils/date";

class WordCategoryController {
    async updateWordCategory(req: Request, res: Response) {
        const {
            itemUid,
            categoryUid,
            syncStatus,
            isDeleted,
            createdAt,
            updatedAt
        } = req.body

        const uid = req.params.uid

        await RepositoryBase.updateOneWithUpsert("note_category", {
            uid: uid
        }, {
            itemUid,
            categoryUid,
            syncStatus: "synced",
            isDeleted: !!isDeleted,
            updatedAt: Number(updatedAt) || date.now(),
            createdAt: Number(createdAt) || date.now(),
        })

        res.status(200).json({ message: "Successfully updated word-category association" })
    }

    async wordCategoryPull(req: Request, res: Response) {
        const since = Number(req.query?.since) || 0
        const query = { updatedAt: { $gt: Number(since) } };
        const items = await RepositoryBase.findAll("note_category", query, {
            sort: { updatedAt: 1 }
        })

        res.json({
            data: items,
            hasMore: items?.length > 0
        });
    }
}

export default (new WordCategoryController())
