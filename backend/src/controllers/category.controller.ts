import RepositoryBase from "../respository/repository.base";
import { Request, Response } from "express"
import date from "../utils/date";

class CategoryController {

    async getCategories(req: Request, res: Response) {
        const items = await RepositoryBase.findAll("category", {})
        res.status(200).json({
            data: items
        })
    }

    async updateCategory(req: Request, res: Response) {
        const {
            name,
            color,
            parentId,
            createdAt,
            updatedAt,
            syncStatus
        } = req.body

        const uid = req.params.uid

        await RepositoryBase.updateOneWithUpsert("category", {
            uid: uid
        }, {
            name,
            color,
            parentId,
            syncStatus: "synced",
            updatedAt: Number(updatedAt) || date.now(),
            createdAt: Number(createdAt) || date.now(),
        })

        res.status(200).json({ message: "Successfully updated" })
    }

    async categoryPull(req: Request, res: Response) {
        const since = Number(req.query?.since) || 0
        const limit = 50
        const query = { updatedAt: { $gt: Number(since) } };
        const items = await RepositoryBase.findAll("category", query, {
            sort: { updatedAt: 1 },
            limit: limit
        })

        res.json({
            data: items,
            hasMore: items?.length > 0
        });
    }
}

export default (new CategoryController())
