// Copyright (c) 2019 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

import { DamlLfValue } from '@da/ui-core';

export const version = {
  schema: 'navigator-config',
  major: 2,
  minor: 0,
};

export const customViews = (userId, party, role) => ({
  issued_ious: {
    type: "table-view",
    title: "Credentials",
    source: {
      type: "contracts",
      filter: [
        {
          field: "argument.issuer",
          value: party,
        },
        {
          field: "template.id",
          value: "Credential:Credential",
        }
      ],
      search: "",
      sort: [
        {
          field: "id",
          direction: "ASCENDING"
        }
      ]
    },
    columns: [
      {
        key: "id",
        title: "Contract ID",
        createCell: ({rowData}) => ({
          type: "text",
          value: rowData.id
        }),
        sortable: true,
        width: 80,
        weight: 0,
        alignment: "left"
      },
      {
        key: "argument.id",
        title: "ID",
        createCell: ({rowData}) => ({
          type: "text",
          value: DamlLfValue.toJSON(rowData.argument).id
        }),
        sortable: true,
        width: 80,
        weight: 3,
        alignment: "left"
      },
      {
        key: "argument.credential",
        title: "credential",
        createCell: ({rowData}) => ({
          type: "text",
          value: DamlLfValue.toJSON(rowData.argument).credential
        }),
        sortable: true,
        width: 80,
        weight: 3,
        alignment: "left"
      }
    ]
  }
})
