import { Dispatch, SetStateAction } from 'react';

export interface FetchOption {
  sort: string;
  page: number;
  perPage: number;
}

export interface SetFetchOption {
  setFetchOption: Dispatch<SetStateAction<FetchOption>>;
}

export interface FetchProp extends SetFetchOption {
  fetchOption: FetchOption;
}
